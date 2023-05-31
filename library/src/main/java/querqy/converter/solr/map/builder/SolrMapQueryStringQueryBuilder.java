package querqy.converter.solr.map.builder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.QueryStringQueryBuilder;

import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class SolrMapQueryStringQueryBuilder implements QueryStringQueryBuilder<Map<String, Object>> {

    @NonNull private final SolrMapQueryReferenceBuilder solrMapQueryReferenceBuilder;

    @Override
    public Map<String, Object> build(final String queryString) {
        final String reference = solrMapQueryReferenceBuilder.createReferenceForQuery(queryString);
        return Map.of("param", reference);
    }
}
