package querqy.converter.elasticsearch.javaclient;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ESJavaClientConverterConfig {

    private static final ESJavaClientConverterConfig DEFAULT = ESJavaClientConverterConfig.builder().build();

    public enum RawQueryInputType {
        JSON, QUERY_STRING_QUERY
    }

    @Builder.Default private final RawQueryInputType rawQueryInputType = RawQueryInputType.QUERY_STRING_QUERY;

    public static ESJavaClientConverterConfig defaultConfig() {
        return DEFAULT;
    }
}
