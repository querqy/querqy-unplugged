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
import querqy.rewriter.builder.ExpandedQueryParser;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Builder
public class QueryRewritingExecutor {

    private final QuerqyConfig querqyConfig;
    @Singular private final Map<String, String[]> params;

    public RewrittenQuerqyQuery rewriteQuery(final String queryInput) {
        final ExpandedQuery parsedQuery =
                ExpandedQueryParser.create().parseQuery(querqyConfig.getQuerqyParserFactory(), queryInput);
        return rewriteQuery(parsedQuery);
    }

    public RewrittenQuerqyQuery rewriteQuery(final Query query) {
        final ExpandedQuery parsedQuery = new ExpandedQuery(query);
        return rewriteQuery(parsedQuery);
    }

    public RewrittenQuerqyQuery rewriteQuery(final ExpandedQuery parsedQuery) {
        final RewriteChain rewriteChain = querqyConfig.getRewriteChain();
        final LocalSearchEngineRequestAdapter requestAdapter = createLocalSearchEngineRequestAdapter(rewriteChain);

        final RewriteChainOutput rewriteChainOutput = rewriteChain.rewrite(parsedQuery, requestAdapter);

        return RewrittenQuerqyQuery.builder()
                .query(rewriteChainOutput.getExpandedQuery())
                .rewriteLogging(extractRewriteLog(rewriteChainOutput))
                .build();
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
