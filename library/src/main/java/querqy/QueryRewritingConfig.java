package querqy;

import lombok.*;
import querqy.rewrite.RewriteChain;
import querqy.rewrite.RewriterFactory;
import querqy.rewrite.commonrules.QuerqyParserFactory;
import querqy.rewrite.commonrules.WhiteSpaceQuerqyParserFactory;

import java.util.List;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
@Getter
public class QueryRewritingConfig {

    private final QuerqyParserFactory querqyParserFactory;
    private final RewriteChain rewriteChain;

    @Builder
    public static QueryRewritingConfig build(
            final QuerqyParserFactory querqyParserFactory, @Singular final List<RewriterFactory> rewriterFactories) {

        if (querqyParserFactory == null) {
            return QueryRewritingConfig.of(new WhiteSpaceQuerqyParserFactory(), new RewriteChain(rewriterFactories));

        } else {
            return QueryRewritingConfig.of(querqyParserFactory, new RewriteChain(rewriterFactories));
        }

    }
}
