package querqy.converter.solr.map.builder;

import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.WrappedQueryBuilder;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class SolrMapWrappedQueryBuilder implements WrappedQueryBuilder<Map<String, Object>> {

    private final SolrMapQueryReferenceBuilder queryReferenceBuilder;

    @Override
    public Map<String, Object> wrap(final Map<String, Object> query) {
        final Map<String, Object> wrappedQuery = new HashMap<>();
        wrappedQuery.put("query", query);
        wrappedQuery.put("queries", queryReferenceBuilder.getReferences());
        return wrappedQuery;
    }
}
