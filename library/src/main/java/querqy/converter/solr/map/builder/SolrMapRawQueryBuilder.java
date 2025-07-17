package querqy.converter.solr.map.builder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.RawQueryBuilder;
import querqy.model.RawQuery;
import querqy.model.StringRawQuery;

import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class SolrMapRawQueryBuilder implements RawQueryBuilder<Map<String, Object>> {

    @NonNull private final SolrMapQueryReferenceBuilder queryReferenceBuilder;

    @Override
    public Map<String, Object> build(final RawQuery rawQuery) {
        if (rawQuery instanceof StringRawQuery) {
            return buildFromString((StringRawQuery) rawQuery);
        } else {
            throw new IllegalArgumentException("Unsupported RawQuery type: " + rawQuery.getClass());
        }
    }

    protected Map<String, Object> buildFromString(final StringRawQuery rawQuery) {
        final String reference = queryReferenceBuilder.createReferenceForQuery(rawQuery.getQueryString());

        return Map.of("param", reference);
    }


}
