package querqy;

import lombok.Builder;
import querqy.adapter.QueryRewritingAdapter;
import querqy.adapter.RewrittenQuery;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.model.ExpandedQuery;

@Builder(toBuilder = true)
public class QueryGenerator<T> {

    private final QueryConfig queryConfig;
    private final QueryRewritingConfig queryRewritingConfig;
    private final ConverterFactory<T> converterFactory;

    public T generateQuery(final String queryInput) {
        final QueryRewritingAdapter adapter = createAdapter(queryInput);
        final RewrittenQuery rewrittenQuery = adapter.rewriteQuery();
        final Converter<T> converter = createConverter(rewrittenQuery.getQuery());

        return converter.convert();
    }

    private QueryRewritingAdapter createAdapter(final String queryInput) {
        return QueryRewritingAdapter.builder()
                .queryInput(queryInput)
                .queryRewritingConfig(queryRewritingConfig)
                .build();
    }

    private Converter<T> createConverter(final ExpandedQuery expandedQuery) {
        return converterFactory.createConverter(
                expandedQuery, queryConfig
        );
    }


}
