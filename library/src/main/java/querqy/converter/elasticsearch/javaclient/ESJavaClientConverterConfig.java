package querqy.converter.elasticsearch.javaclient;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ESJavaClientConverterConfig {

    public enum RawQueryInput {
        JSON, QUERY_STRING_QUERY
    }

    @Builder.Default private final RawQueryInput rawQueryInput = RawQueryInput.QUERY_STRING_QUERY;
}
