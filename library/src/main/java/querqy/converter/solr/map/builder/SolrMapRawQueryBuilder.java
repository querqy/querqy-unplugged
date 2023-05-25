package querqy.converter.solr.map.builder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.RawQueryBuilder;

import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class SolrMapRawQueryBuilder implements RawQueryBuilder<Map<String, Object>> {

    @NonNull private final SolrMapQueryReferenceBuilder queryReferenceBuilder;

    @Override
    public Map<String, Object> buildFromString(final String rawQueryString) {
        final String reference = queryReferenceBuilder.createReferenceForQuery(rawQueryString);

        return Map.of("param", reference);
    }
}
