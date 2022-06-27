package querqy;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import querqy.rewrite.RewriteChain;
import querqy.rewrite.RewriterFactory;
import querqy.rewrite.commonrules.QuerqyParserFactory;
import querqy.rewrite.commonrules.WhiteSpaceQuerqyParserFactory;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
@Getter
public class QueryRewritingConfig {

    private final QuerqyParserFactory querqyParserFactory;
    private final RewriteChain rewriteChain;

    @Builder
    public static QueryRewritingConfig build(
            final QuerqyParserFactory querqyParserFactory, @Singular final List<RewriterFactory> rewriterFactories) {

        return QueryRewritingConfig.of(
                Objects.requireNonNullElseGet(querqyParserFactory, WhiteSpaceQuerqyParserFactory::new),
                new RewriteChain(rewriterFactories));

    }
}
