package querqy.converter.solr.map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Deprecated
public class MapConverterConfig {

    @Builder.Default private final String fieldNodeName = "field";

    public static MapConverterConfig defaultConfig() {
        return MapConverterConfig.builder().build();
    }

}
