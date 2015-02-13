package cn.edu.scut.kapok.distributed.worker.retriever;

import cn.edu.scut.kapok.distributed.common.util.ByteBufferInputStream;
import cn.edu.scut.kapok.distributed.protos.QueryProto.Query;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryRequest;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResponse;
import cn.edu.scut.kapok.distributed.protos.QueryProto.QueryResult;
import cn.edu.scut.kapok.distributed.worker.spi.RetrieveException;
import cn.edu.scut.kapok.distributed.worker.spi.Retriever;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.edu.scut.kapok.distributed.protos.QueryProto.Query.Type;
import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class BBTRetriever implements Retriever {

    private final HttpClient client;

    @Inject
    public BBTRetriever(HttpClient client) {
        this.client = client;
    }

    private List<String> extractWords(Query query) {
        // Skip not condition.
        if (query.hasNot() && query.getNot()) {
            return Collections.emptyList();
        }
        if (query.getType() == Type.WORD) {
            return Lists.newArrayList(query.getWordQuery().getWord());
        } else if (query.getType() == Type.LOGIC) {
            List<String> words = new ArrayList<>();
            for (Query q : query.getBooleanQuery().getQueriesList()) {
                words.addAll(extractWords(q));
            }
            return words;
        } else {
            // Unknown query type.
            return Collections.emptyList();
        }
    }

    private BBTResultList extractResult(InputStream in) throws IOException {
        BBTResultList results = new BBTResultList();
        Document doc;
        doc = Jsoup.parse(in, null, "http://www.100steps.net/index.php");
        String introT = doc.select("div.searchintro p strong").text();
        int total = Integer.valueOf(introT.split("//s+")[1]);
        results.setTotal(total);
        for (Element elem : doc.select(".news_msg")) {
            Element titleA = elem.select("h4 a").first();
            String url = titleA.absUrl("href");
            String title = titleA.text();
            Element descP = elem.select("p.mod-articles-category-introtest").first();
            String desc = descP.text();
            Element dateSpan = elem.select("div.date span").first();
            String date = dateSpan.text();
            BBTResult result = new BBTResult();
            result.url = url;
            result.title = title;
            result.desc = desc;
            result.date = date;
            results.addResult(result);
        }
        return results;
    }

    @Override
    public ListenableFuture<QueryResponse> retrieve(QueryRequest request) {
        checkNotNull(request);

        final SettableFuture<QueryResponse> future = SettableFuture.create();

        List<String> words = extractWords(request.getQuery());
        String query = Joiner.on(" ").join(words);
        int from = request.getFrom();
        int count = request.getCount();
        String url = String.format(
                "http://www.100steps.net/index.php?searchword=%s&searchphrase=all&limit=%d&option=com_search&limitstart=%d",
                query, count, from);

        client.newRequest(url).onResponseContent(new Response.ContentListener() {
            @Override
            public void onContent(Response response, ByteBuffer content) {
                if (response.getStatus() != HttpStatus.OK_200) {
                    // error set in onComplete.
                    return;
                }
                InputStream in = new ByteBufferInputStream(content);
                try {
                    BBTResultList results = extractResult(in);
                    QueryResponse.Builder builder = QueryResponse.newBuilder();
                    builder.setTotal(results.getTotal());
                    for (BBTResult result : results.getResults()) {
                        QueryResult.Builder b = builder.addResultsBuilder();
                        b.addFieldsBuilder().setName("url").setValue(result.url).build();
                        b.addFieldsBuilder().setName("title").setValue(result.title).build();
                        b.addFieldsBuilder().setName("desc").setValue(result.desc).build();
                        b.addFieldsBuilder().setName("date").setValue(result.date).build();
                        b.build();
                    }
                    QueryResponse resp = builder.build();
                    future.set(resp);
                } catch (IOException e) {
                    future.setException(e);
                }
            }
        }).send(new Response.CompleteListener() {
            @Override
            public void onComplete(Result result) {
                if (result.isFailed()) {
                    future.setException(result.getFailure());
                }
                if (result.getResponse().getStatus() != HttpStatus.OK_200) {
                    future.setException(new RetrieveException("baidu response code error"));
                }
            }
        });

        return future;
    }

    private static class BBTResult {
        public String url;
        public String title;
        public String desc;
        public String date;
    }

    private static class BBTResultList {
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
