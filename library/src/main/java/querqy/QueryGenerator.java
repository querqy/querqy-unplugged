package querqy;

import lombok.Builder;
import querqy.adapter.QueryRewritingAdapter;
import querqy.domain.RewrittenQuerqyQuery;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.domain.RewrittenQuery;
import querqy.model.ExpandedQuery;

@Builder(toBuilder = true)
public class QueryGenerator<T> {

    private final QueryConfig queryConfig;
    private final QueryRewritingConfig queryRewritingConfig;
    private final ConverterFactory<T> converterFactory;

    public RewrittenQuery<T> generateQuery(final String queryInput) {
        final QueryRewritingAdapter adapter = createAdapter(queryInput);
        final RewrittenQuerqyQuery rewrittenQuerqyQuery = adapter.rewriteQuery();
        final Converter<T> converter = createConverter(rewrittenQuerqyQuery.getQuery());

        final T convertedQuery = converter.convert();
        return RewrittenQuery.<T>builder()
                .convertedQuery(convertedQuery)
                .rewrittenQuerqyQuery(rewrittenQuerqyQuery)
                .build();
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
