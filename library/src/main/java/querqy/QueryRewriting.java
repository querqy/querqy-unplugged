package querqy;

import lombok.Builder;
import querqy.rewriter.QueryRewritingExecutor;
import querqy.domain.RewrittenQuerqyQuery;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.domain.RewrittenQuery;
import querqy.model.ExpandedQuery;

@Builder(toBuilder = true)
public class QueryRewriting<T> {

    private final QueryConfig queryConfig;
    private final QueryRewritingConfig queryRewritingConfig;
    private final ConverterFactory<T> converterFactory;

    public RewrittenQuery<T> rewriteQuery(final String queryInput) {
        final QueryRewritingExecutor executor = createExecutor(queryInput);
        final RewrittenQuerqyQuery rewrittenQuerqyQuery = executor.rewriteQuery();
        final Converter<T> converter = createConverter(rewrittenQuerqyQuery.getQuery());

        final T convertedQuery = converter.convert();
        return RewrittenQuery.<T>builder()
                .convertedQuery(convertedQuery)
                .rewrittenQuerqyQuery(rewrittenQuerqyQuery)
                .build();
    }

    private QueryRewritingExecutor createExecutor(final String queryInput) {
        return QueryRewritingExecutor.builder()
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
