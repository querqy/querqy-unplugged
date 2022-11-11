package querqy.rewriter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Singular;
import querqy.QuerqyConfig;
import querqy.domain.RewrittenQuerqyQuery;
import querqy.model.ExpandedQuery;
import querqy.model.Query;
import querqy.parser.QuerqyParser;
import querqy.rewrite.RewriteChain;
import querqy.rewrite.RewriteChainOutput;
import querqy.rewrite.RewriteLoggingConfig;
import querqy.rewrite.commonrules.QuerqyParserFactory;
import querqy.rewrite.logging.RewriteChainLog;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Builder
public class QueryRewritingExecutor {

    private final String queryInput;
    private final QuerqyConfig queryRewritingConfig;

    @Singular private final Map<String, String[]> params;
    @Builder.Default final RewriteLoggingConfig rewriteLoggingConfig = RewriteLoggingConfig.off();

    public RewrittenQuerqyQuery rewriteQuery() {
        final ExpandedQuery parsedQuery = parseQuery();
        final LocalSearchEngineRequestAdapter requestAdapter = createLocalSearchEngineRequestAdapter();

        final RewriteChain rewriteChain = queryRewritingConfig.getRewriteChain();
        final RewriteChainOutput rewriteChainOutput = rewriteChain.rewrite(parsedQuery, requestAdapter);

        return RewrittenQuerqyQuery.builder()
                .query(rewriteChainOutput.getExpandedQuery())
                .rewriteLogging(extractRewriteLog(rewriteChainOutput))
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
                .rewriteLoggingConfig(rewriteLoggingConfig)
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
