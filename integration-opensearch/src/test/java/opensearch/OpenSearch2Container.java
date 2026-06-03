package opensearch;

import org.opensearch.testcontainers.OpenSearchContainer;
import org.testcontainers.utility.DockerImageName;

public class OpenSearch2Container {

    private static OpenSearchContainer<?> openSearchContainer = null;

    // TODO: maintain in build.gradle?
    private static final String OPENSEARCH_DOCKER = "opensearchproject/opensearch:2.19.0";

    private OpenSearch2Container() {
    }

    public static OpenSearchContainer<?> createOpenSearchContainer() {
        if (openSearchContainer == null) {
            openSearchContainer = new OpenSearchContainer<>(DockerImageName.parse(OPENSEARCH_DOCKER));
        }

        return openSearchContainer;
    }
}
