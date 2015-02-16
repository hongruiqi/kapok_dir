package cn.edu.scut.kapok.distributed.worker.retriever.impl;

import cn.edu.scut.kapok.distributed.protos.QueryProto.Query;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResult;
import cn.edu.scut.kapok.distributed.worker.retriever.spi.RetrieveException;
import cn.edu.scut.kapok.distributed.worker.retriever.spi.Retriever;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class BBTRetriever implements Retriever {

    private final Logger logger = LoggerFactory.getLogger(BBTRetriever.class);

    private final HttpAsyncClient httpClient;

    private static final String BBTSearchURL = "http://www.100steps.net/index.php?searchword=%s&searchphrase=all&limit=%d&option=com_search&limitstart=%d";

    @Inject
    public BBTRetriever(HttpAsyncClient httpClient) {
        this.httpClient = httpClient;
    }

    public static String getBBTSearchURL() {
        return BBTSearchURL;
    }

    List<String> extractWords(Query query) {
        // Skip not condition.
        if (query.hasNot() && query.getNot()) {
            return Collections.emptyList();
        }
        switch (query.getQueryOneofCase()) {
            case WORD_QUERY:
                return Lists.newArrayList(query.getWordQuery().getWord());
            case BOOLEAN_QUERY:
                List<String> words = new ArrayList<>();
                for (Query q : query.getBooleanQuery().getQueriesList()) {
                    words.addAll(extractWords(q));
                }
                return words;
            default:
                return Collections.emptyList();
        }
    }

    BBTResultList extractResult(InputStream in) throws IOException {
        BBTResultList results = new BBTResultList();
        Document doc;
        doc = Jsoup.parse(in, null, "http://www.100steps.net/index.php");
        String introT = doc.select("div.searchintro p strong").text();
        int total = Integer.valueOf(introT.replaceAll("[^0-9]+", ""));
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

    String makeSearchUrl(int from, int count, String query) {
        try {
            return String.format(BBTSearchURL,
                    URLEncoder.encode(query, "utf8"), count, from);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ListenableFuture<QueryResponse> retrieve(QueryRequest request) {
        checkNotNull(request);
        final SettableFuture<QueryResponse> future = SettableFuture.create();
        try {
            _retrieve(request, future);
        } catch (Throwable t) {
            future.setException(t);
        }
        return future;
    }

    private void _retrieve(QueryRequest queryRequest, final SettableFuture<QueryResponse> future) throws RetrieveException {
        List<String> words = extractWords(queryRequest.getQuery());
        String query = Joiner.on(" ").join(words);

        int from = queryRequest.getFrom();
        int count = queryRequest.getCount();

        String url = makeSearchUrl(from, count, query);
        HttpGet request = new HttpGet(url);
        httpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                if (result.getStatusLine().getStatusCode() != org.apache.http.HttpStatus.SC_OK) {
                    future.setException(new RetrieveException("bbt response code error"));
                    return;
                }
                BBTResultList results;
                try {
                    results = extractResult(result.getEntity().getContent());
                } catch (Throwable e) {
                    future.setException(new RetrieveException(e));
                    return;
                }
                QueryResponse resp = bbtResultsToQueryResponse(results);
                future.set(resp);
            }

            @Override
            public void failed(Exception ex) {
                future.setException(new RetrieveException(ex));
            }

            @Override
            public void cancelled() {
                future.cancel(true);
            }
        });
    }

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

    public static class BBTResult {
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

    public static class BBTResultList {
        private List<BBTResult> results = new ArrayList<>();
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
