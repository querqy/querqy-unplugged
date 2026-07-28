package elasticsearch;

import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public class Elasticsearch7Container extends ElasticsearchContainer {

    private static ElasticsearchContainer elasticsearchContainer = null;

    // TODO: maintain in build.gradle?
    private static final String ELASTIC_SEARCH_DOCKER = "elasticsearch:7.17.8";

    private static final String CLUSTER_NAME = "cluster.name";

    private static final String ELASTIC_SEARCH = "elasticsearch";

    private Elasticsearch7Container() {
        super(DockerImageName.parse(ELASTIC_SEARCH_DOCKER)
                .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"));
        this.addEnv(CLUSTER_NAME, ELASTIC_SEARCH);
    }

    public static ElasticsearchContainer createElasticsearchContainer() {
        if (elasticsearchContainer == null) {
            elasticsearchContainer = new Elasticsearch7Container();
        }

        return elasticsearchContainer;
    }
}
