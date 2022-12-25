package querqy.converter.solr.map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Deprecated
public class MapConverterConfig {

    @Builder.Default private final String boolNodeName = "bool";
    @Builder.Default private final String disMaxNodeName = "nestedDismax";
    @Builder.Default private final String scoringNodeName = "constantScore";
    @Builder.Default private final String matchingNodeName = "field";

    public static MapConverterConfig defaultConfig() {
        return MapConverterConfig.builder().build();
    }

}
