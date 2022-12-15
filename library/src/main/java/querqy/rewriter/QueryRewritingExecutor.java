package querqy.rewriter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Singular;
import querqy.QuerqyConfig;
import querqy.domain.RewrittenQuerqyQuery;
import querqy.model.ExpandedQuery;
import querqy.model.MatchAllQuery;
import querqy.model.Query;
import querqy.parser.QuerqyParser;
import querqy.rewrite.RewriteChain;
import querqy.rewrite.RewriteChainOutput;
import querqy.rewrite.commonrules.QuerqyParserFactory;
import querqy.rewrite.logging.RewriteChainLog;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Builder
public class QueryRewritingExecutor {

    public static final String MATCH_ALL_FIELD_VALUE_INPUT = "*:*";
    public static final String MATCH_ALL_WILDCARD_INPUT = "*";

    private final String queryInput;
    private final QuerqyConfig querqyConfig;

    @Singular private final Map<String, String[]> params;

    public RewrittenQuerqyQuery rewriteQuery() {
        final ExpandedQuery parsedQuery = parseQuery();
        final RewriteChain rewriteChain = querqyConfig.getRewriteChain();
        final LocalSearchEngineRequestAdapter requestAdapter = createLocalSearchEngineRequestAdapter(rewriteChain);

        final RewriteChainOutput rewriteChainOutput = rewriteChain.rewrite(parsedQuery, requestAdapter);

        return RewrittenQuerqyQuery.builder()
                .query(rewriteChainOutput.getExpandedQuery())
                .rewriteLogging(extractRewriteLog(rewriteChainOutput))
                .build();
    }

    private ExpandedQuery parseQuery() {
        if (MATCH_ALL_WILDCARD_INPUT.equals(queryInput) || MATCH_ALL_FIELD_VALUE_INPUT.equals(queryInput)) {
            return new ExpandedQuery(new MatchAllQuery());

        } else {
            return parseRegularQuery();
        }
    }

    private ExpandedQuery parseRegularQuery() {
        final QuerqyParserFactory parserFactory = querqyConfig.getQuerqyParserFactory();
        final QuerqyParser parser = parserFactory.createParser();

        final Query parsedQuery = parser.parse(queryInput);
        return new ExpandedQuery(parsedQuery);
    }

    private LocalSearchEngineRequestAdapter createLocalSearchEngineRequestAdapter(final RewriteChain rewriteChain) {
        return LocalSearchEngineRequestAdapter.builder()
                .rewriteChain(rewriteChain)
                .params(params)
                .rewriteLoggingConfig(querqyConfig.getRewriteLoggingConfig())
                .build();
    }

    private Map<String, Object> extractRewriteLog(final RewriteChainOutput rewriteChainOutput) {
        final Optional<RewriteChainLog> rewriteLog = rewriteChainOutput.getRewriteLog();
        return rewriteLog.isPresent() ? convertRewriteLog(rewriteLog.get()) : Collections.emptyMap();
    }

    private Map<String, Object> convertRewriteLog(final RewriteChainLog rewriteChainLog) {
        final ObjectMapper objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        return objectMapper.convertValue(rewriteChainLog, new TypeReference<>() {});
    }

}
