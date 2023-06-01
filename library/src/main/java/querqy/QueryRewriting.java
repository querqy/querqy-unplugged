package querqy;

import lombok.Builder;
import lombok.NonNull;
import querqy.model.ExpandedQuery;
import querqy.model.Query;
import querqy.rewriter.QueryRewritingExecutor;
import querqy.domain.RewrittenQuerqyQuery;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.domain.RewrittenQuery;

@Builder(toBuilder = true)
public class QueryRewriting<T> {

    @NonNull private final QueryConfig queryConfig;
    @Builder.Default private final QuerqyConfig querqyConfig = QuerqyConfig.empty();
    @NonNull private final ConverterFactory<T> converterFactory;

    @Builder.Default private final QueryExpansionConfig<T> queryExpansionConfig = QueryExpansionConfig.empty();

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
        final Converter<T> converter = converterFactory.createConverter(queryConfig, queryExpansionConfig);

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
