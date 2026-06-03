package querqy.converter.opensearch.javaclient;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OSJavaClientConverterConfig {

    private static final OSJavaClientConverterConfig DEFAULT = OSJavaClientConverterConfig.builder().build();

    public enum RawQueryInputType {
        JSON, QUERY_STRING_QUERY
    }

    @Builder.Default private final RawQueryInputType rawQueryInputType = RawQueryInputType.QUERY_STRING_QUERY;

    public static OSJavaClientConverterConfig defaultConfig() {
        return DEFAULT;
    }
}
