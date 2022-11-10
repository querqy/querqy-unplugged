package querqy.rewriter;

import lombok.Builder;
import querqy.QueryRewritingConfig;
import querqy.domain.RewrittenQuerqyQuery;
import querqy.model.ExpandedQuery;
import querqy.model.Query;
import querqy.parser.QuerqyParser;
import querqy.rewrite.RewriteChain;
import querqy.rewrite.commonrules.QuerqyParserFactory;

import java.util.HashMap;
import java.util.Map;

@Builder
public class QueryRewritingExecutor {

    private final String queryInput;
    private final QueryRewritingConfig queryRewritingConfig;

    @Builder.Default private final Map<String, String[]> params = new HashMap<>();

    // TODO: make configurable whether logging is used


    public RewrittenQuerqyQuery rewriteQuery() {
        final ExpandedQuery parsedQuery = parseQuery();
        final LocalSearchEngineRequestAdapter requestAdapter = createLocalSearchEngineRequestAdapter();

        final RewriteChain rewriteChain = queryRewritingConfig.getRewriteChain();
        final ExpandedQuery rewrittenQuery = rewriteChain.rewrite(parsedQuery, requestAdapter);

        return RewrittenQuerqyQuery.builder()
                .query(rewrittenQuery)
                .rewritingTracking(requestAdapter.getRewritingTracking())
                .build();
    }

    private ExpandedQuery parseQuery() {
        final QuerqyParserFactory parserFactory = queryRewritingConfig.getQuerqyParserFactory();
        final QuerqyParser parser = parserFactory.createParser();

        final Query parsedQuery = parser.parse(queryInput);
        return new ExpandedQuery(parsedQuery);
    }

    private LocalSearchEngineRequestAdapter createLocalSearchEngineRequestAdapter() {
        return LocalSearchEngineRequestAdapter.builder()
                .rewriteChain(queryRewritingConfig.getRewriteChain())
                .params(params)
                .hasActiveInfoLogging(true)
                .hasActiveActionTracking(true)
                .build();
    }

}
