package querqy;

import lombok.Builder;
import querqy.model.ExpandedQuery;
import querqy.model.Query;
import querqy.rewriter.QueryRewritingExecutor;
import querqy.domain.RewrittenQuerqyQuery;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.domain.RewrittenQuery;

@Builder(toBuilder = true)
public class QueryRewriting<T> {

    private final QueryConfig queryConfig;
    private final QuerqyConfig querqyConfig;
    private final ConverterFactory<T> converterFactory;

    public RewrittenQuery<T> rewriteQuery(final String queryInput) {
        final QueryRewritingExecutor executor = createExecutor();
        final RewrittenQuerqyQuery rewrittenQuerqyQuery = executor.rewriteQuery(queryInput);
        return convertQuery(rewrittenQuerqyQuery);
    }

    public RewrittenQuery<T> rewriteQuery(final Query query) {
        final QueryRewritingExecutor executor = createExecutor();
        final RewrittenQuerqyQuery rewrittenQuerqyQuery = executor.rewriteQuery(query);
        return convertQuery(rewrittenQuerqyQuery);
    }

    public RewrittenQuery<T> rewriteQuery(final ExpandedQuery query) {
        final QueryRewritingExecutor executor = createExecutor();
        final RewrittenQuerqyQuery rewrittenQuerqyQuery = executor.rewriteQuery(query);
        return convertQuery(rewrittenQuerqyQuery);
    }

    private RewrittenQuery<T> convertQuery(final RewrittenQuerqyQuery rewrittenQuerqyQuery) {
        final Converter<T> converter = converterFactory.createConverter(queryConfig);

        final T convertedQuery = converter.convert(rewrittenQuerqyQuery.getQuery());
        return RewrittenQuery.<T>builder()
                .convertedQuery(convertedQuery)
                .rewrittenQuerqyQuery(rewrittenQuerqyQuery)
                .build();
    }

    private QueryRewritingExecutor createExecutor() {
        return QueryRewritingExecutor.builder()
                .querqyConfig(querqyConfig)
                .build();
    }

}
