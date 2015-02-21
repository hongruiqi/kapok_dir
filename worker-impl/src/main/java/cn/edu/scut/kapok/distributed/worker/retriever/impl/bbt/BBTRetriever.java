package cn.edu.scut.kapok.distributed.worker.retriever.impl.bbt;

import cn.edu.scut.kapok.distributed.protos.Query;
import cn.edu.scut.kapok.distributed.protos.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.QueryResult;
import cn.edu.scut.kapok.distributed.worker.api.retriever.RetrieveException;
import cn.edu.scut.kapok.distributed.worker.api.retriever.Retriever;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class BBTRetriever implements Retriever {

    private static final String BBTSearchURL = "http://www.100steps.net/index.php?searchword=%s&searchphrase=all&limit=%d&option=com_search&limitstart=%d";
    private static final String BBTBaseURL = "http://www.100steps.net/index.php";

    private final HttpAsyncClient httpClient;

    /**
     * Create a new BBTRetriever.
     *
     * @param httpClient Client used to communicate with bbt server.
     */
    @Inject
    public BBTRetriever(HttpAsyncClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Extract all words from {@code query}. Queries with {@code not} set are ignored.
     *
     * @param query Query to be extracted.
     * @return List of extracted words.
     */
    List<String> extractWords(Query query) {
        // Ignore not queries.
        if (query.hasNot() && query.getNot()) {
            return Collections.emptyList();
        }
        switch (query.getQueryOneofCase()) {
            case WORD_QUERY:
                return Lists.newArrayList(query.getWordQuery().getWord());
            case BOOLEAN_QUERY:
                List<String> words = new ArrayList<>();
                for (Query q : query.getBooleanQuery().getQueriesList()) {
                    // ExtractWords from subQuery.
                    words.addAll(extractWords(q));
                }
                return words;
            default:
                // Unknown query type, just return emtpy list.
                return Collections.emptyList();
        }
    }

    /**
     * Extract results from {@code in}.
     *
     * @param in InputStream to be extracted from.
     * @return Results that is extracted.
     * @throws IOException
     */
    BBTResultList extractResult(InputStream in) throws IOException {
        BBTResultList results = new BBTResultList();
        Document doc = Jsoup.parse(in, null, BBTBaseURL);
        String introT = doc.select("div.searchintro p strong").text();
        int total = Integer.parseInt(introT.replaceAll("[^0-9]+", ""));
        results.setTotal(total);
        for (Element elem : doc.select(".news_msg")) {
            Element titleA = elem.select("h4 a").first();
            String url = titleA.absUrl("href");
            String title = titleA.text();
            Element descP = elem.select("p.mod-articles-category-introtest").first();
            String desc = descP.text();
            Element dateSpan = elem.select("div.date span").first();
            String date = dateSpan.text();
            BBTResult result = new BBTResult(url, title, desc, date);
            results.addResult(result);
        }
        return results;
    }

    /**
     * Return composed url from params.
     * {@code query} is urlencoded.
     *
     * @param from  Start offset of the result to be fetched.
     * @param count Number of the results to be fetched.
     * @param query Query string.
     * @return Composed url.
     */
    String makeSearchUrl(int from, int count, String query) {
        try {
            return String.format(BBTSearchURL,
                    URLEncoder.encode(query, "utf8"), count, from);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrive QueryResponse use {@code queryRequest}.
     *
     * @param queryRequest QueryRequest to be retrieved.
     * @return ListenableFuture is done when response is retrieved or error occured.
     */
    @Override
    public ListenableFuture<QueryResponse> retrieve(QueryRequest queryRequest) {
        final SettableFuture<QueryResponse> future = SettableFuture.create();

        // Generate query.
        List<String> words = extractWords(queryRequest.getQuery());
        String query = Joiner.on(" ").join(words);

        int from = queryRequest.getFrom();
        int count = queryRequest.getCount();
        String url = makeSearchUrl(from, count, query);

        HttpGet request = new HttpGet(url);
        httpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                try {
                    int statusCode = result.getStatusLine().getStatusCode();
                    if (statusCode != org.apache.http.HttpStatus.SC_OK) {
                        throw new UnexpectedStatusCodeException(statusCode);
                    }
                    BBTResultList results = extractResult(result.getEntity().getContent());
                    QueryResponse resp = bbtResultsToQueryResponse(results);
                    future.set(resp);
                } catch (RetrieveException e) {
                    future.setException(e);
                } catch (Throwable t) {
                    future.setException(new RetrieveException(t));
                }
            }

            @Override
            public void failed(Exception ex) {
                // Wrap ex in a RetrieveFailedException.
                future.setException(new RetrieveFailedException(ex));
            }

            @Override
            public void cancelled() {
                future.cancel(true);
            }
        });

        return future;
    }

    /**
     * Transform BBTResultList to QueryResponse.
     *
     * @param results BBTResultList
     * @return transformed QueryResponse
     */
    QueryResponse bbtResultsToQueryResponse(BBTResultList results) {
        QueryResponse.Builder builder = QueryResponse.newBuilder();
        builder.setTotal(results.getTotal());
        for (BBTResult r : results.getResults()) {
            QueryResult.Builder b = builder.addResultsBuilder();
            b.addFieldsBuilder().setName("url").setValue(r.url).build();
            b.addFieldsBuilder().setName("title").setValue(r.title).build();
            b.addFieldsBuilder().setName("desc").setValue(r.desc).build();
            b.addFieldsBuilder().setName("date").setValue(r.date).build();
        }
        return builder.build();
    }

    private static class BBTResult {
        public String url = "";
        public String title = "";
        public String desc = "";
        public String date = "";

        public BBTResult(String url, String title, String desc, String date) {
            this.url = url;
            this.title = title;
            this.desc = desc;
            this.date = date;
        }
    }

    private static class BBTResultList {
        private final List<BBTResult> results = new ArrayList<>();
        private int total = 0;

        public void addResult(BBTResult result) {
            results.add(result);
        }

        public List<BBTResult> getResults() {
            return results;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }
}
