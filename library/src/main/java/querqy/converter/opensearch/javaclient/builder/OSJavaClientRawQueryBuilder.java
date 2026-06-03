package querqy.converter.opensearch.javaclient.builder;

import jakarta.json.stream.JsonParser;
import lombok.RequiredArgsConstructor;

import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryStringQuery;
import querqy.converter.opensearch.javaclient.OSJavaClientConverterConfig;
import querqy.converter.opensearch.javaclient.OpenSearchDSLRawQuery;
import querqy.converter.generic.builder.RawQueryBuilder;
import querqy.model.RawQuery;
import querqy.model.StringRawQuery;

import java.io.StringReader;

@RequiredArgsConstructor(staticName = "of")
public class OSJavaClientRawQueryBuilder implements RawQueryBuilder<Query> {

    private static final JsonpMapper MAPPER = new JacksonJsonpMapper();

    private final OSJavaClientConverterConfig converterConfig;

    @Override
    public Query build(final RawQuery rawQuery) {
        if (rawQuery instanceof StringRawQuery) {
            return buildFromStringRawQuery((StringRawQuery) rawQuery);
        } else if (rawQuery instanceof OpenSearchDSLRawQuery) {
            return ((OpenSearchDSLRawQuery) rawQuery).getQuery();
        } else {
            throw new IllegalArgumentException("Unsupported RawQuery type: " + rawQuery.getClass());
        }
    }
    protected Query buildFromStringRawQuery(final StringRawQuery rawQuery) {
        final String rawQueryString = rawQuery.getQueryString();
        switch (converterConfig.getRawQueryInputType()) {

            case QUERY_STRING_QUERY: return parseAsQueryStringQuery(rawQueryString);
            case JSON: return parseAsJson(rawQueryString);

            default:
                throw new IllegalArgumentException(this.getClass().getName() + " does not support raw query input type " +
                        converterConfig.getRawQueryInputType().name());
        }
    }

    private Query parseAsQueryStringQuery(final String rawQueryString) {
        return new Query(new QueryStringQuery.Builder().query(rawQueryString).build());
    }

    private Query parseAsJson(final String rawQueryString) {
        try (final JsonParser parser = MAPPER.jsonProvider().createParser(new StringReader(rawQueryString))) {
            return Query._DESERIALIZER.deserialize(parser, MAPPER);
        }
    }
}
