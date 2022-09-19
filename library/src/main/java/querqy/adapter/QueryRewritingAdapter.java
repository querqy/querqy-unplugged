package querqy.adapter;

import lombok.Builder;
import querqy.QueryRewritingConfig;
import querqy.infologging.InfoLoggingContext;
import querqy.model.ExpandedQuery;
import querqy.model.Query;
import querqy.parser.QuerqyParser;
import querqy.rewrite.RewriteChain;
import querqy.rewrite.commonrules.QuerqyParserFactory;

import java.util.HashMap;
import java.util.Map;

@Builder
public class QueryRewritingAdapter {

    private final String queryInput;
    private final QueryRewritingConfig queryRewritingConfig;

    @Builder.Default private final Map<String, String[]> params = new HashMap<>();

    private final LocalInfoLogging localInfoLogging = LocalInfoLogging.create();

    public RewrittenQuery rewriteQuery() {
        final ExpandedQuery parsedQuery = parseQuery();
        final LocalSearchEngineRequestAdapter requestAdapter = createLocalSearchEngineRequestAdapter();

        final RewriteChain rewriteChain = queryRewritingConfig.getRewriteChain();
        final ExpandedQuery rewrittenQuery = rewriteChain.rewrite(parsedQuery, requestAdapter);

        return RewrittenQuery.of(
                rewrittenQuery,
                localInfoLogging.getRewritingActions()
        );
    }

    private ExpandedQuery parseQuery() {
        final QuerqyParserFactory parserFactory = queryRewritingConfig.getQuerqyParserFactory();
        final QuerqyParser parser = parserFactory.createParser();

        final Query parsedQuery = parser.parse(queryInput);
        return new ExpandedQuery(parsedQuery);
    }

    private LocalSearchEngineRequestAdapter createLocalSearchEngineRequestAdapter() {
        final LocalSearchEngineRequestAdapter requestAdapter = LocalSearchEngineRequestAdapter.builder()
                .rewriteChain(queryRewritingConfig.getRewriteChain())
                .params(params)
                .build();

        final InfoLoggingContext infoLoggingContext = new InfoLoggingContext(localInfoLogging, requestAdapter);
        requestAdapter.setInfoLoggingContext(infoLoggingContext);

        return requestAdapter;
    }

}
