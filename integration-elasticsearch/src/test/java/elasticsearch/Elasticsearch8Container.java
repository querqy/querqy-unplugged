package elasticsearch;

import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public class Elasticsearch8Container extends ElasticsearchContainer {

    private static ElasticsearchContainer elasticsearchContainer = null;

    // TODO: maintain in build.gradle?
    private static final String ELASTIC_SEARCH_DOCKER = "elasticsearch:8.6.0";

    private static final String CLUSTER_NAME = "cluster.name";

    private static final String ELASTIC_SEARCH = "elasticsearch";

    private Elasticsearch8Container() {
        super(DockerImageName.parse(ELASTIC_SEARCH_DOCKER)
                .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"));
        this.addFixedExposedPort(9201, 9201);
        this.addFixedExposedPort(9301, 9301);
        this.addEnv(CLUSTER_NAME, ELASTIC_SEARCH);
    }

    public static ElasticsearchContainer createElasticsearchContainer() {
        if (elasticsearchContainer == null) {
            elasticsearchContainer = new Elasticsearch8Container();
        }

        return elasticsearchContainer;
    }
}
