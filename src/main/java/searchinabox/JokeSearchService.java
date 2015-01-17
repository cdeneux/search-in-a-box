package searchinabox;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class JokeSearchService {

    @Autowired
    private Client client;

    @PostConstruct
    public void indexJokes() throws IOException {
        // create an index name "jokes" to store the jokes in
        try {
            if (!client.admin().indices().prepareCreate("jokes").execute().actionGet().isAcknowledged()) {
                throw new IllegalStateException("failed to create index");
            }
        } catch (IndexAlreadyExistsException ignored) {
        }

        storeJoke(1, "Why are teddy bears never hungry? ", "They are always stuffed!");
        storeJoke(2, "Where do polar bears vote? ", "The North Poll!");
    }

    private void storeJoke(int id, String question, String answer) throws IOException {
        // index a document ID  of type "joke" in the "jokes" index
        if (client.prepareIndex("jokes", "joke", String.valueOf(id))
                .setSource(
                        XContentFactory.jsonBuilder()
                                .startObject()
                                .field("question", question)
                                .field("answer", answer)
                                .endObject()
                )
                .execute()
                .actionGet().isCreated()) {
            throw new IllegalStateException("joke not created");
        }
    }

    public SearchHit[] search(String query) {
        return client.prepareSearch("jokes")
                .setTypes("joke")
                .setQuery(QueryBuilders.multiMatchQuery(query, "question", "answer"))
                .execute()
                .actionGet().getHits().getHits();
    }
}
