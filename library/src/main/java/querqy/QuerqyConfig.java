package querqy;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import querqy.rewrite.RewriteChain;
import querqy.rewrite.RewriteLoggingConfig;
import querqy.rewrite.RewriterFactory;
import querqy.rewrite.commonrules.QuerqyParserFactory;
import querqy.rewrite.commonrules.WhiteSpaceQuerqyParserFactory;
import querqy.rewriter.builder.CommonRulesDefinition;
import querqy.rewriter.builder.CommonRulesFactoryBuilder;
import querqy.rewriter.builder.ReplaceRulesDefinition;
import querqy.rewriter.builder.ReplaceRulesFactoryBuilder;
import querqy.rewriter.builder.RewriterSupport;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
@Getter
public class QuerqyConfig {

    private final QuerqyParserFactory querqyParserFactory;
    private final RewriteChain rewriteChain;

    private final RewriteLoggingConfig rewriteLoggingConfig;

    @Builder
    public static QuerqyConfig build(
            final QuerqyParserFactory querqyParserFactory,
            @Singular final List<RewriterFactory> rewriterFactories,
            final RewriteLoggingConfig rewriteLoggingConfig
    ) {
        return QuerqyConfig.of(
                Objects.requireNonNullElseGet(querqyParserFactory, WhiteSpaceQuerqyParserFactory::new),
                new RewriteChain(rewriterFactories),
                Objects.requireNonNullElseGet(rewriteLoggingConfig, RewriteLoggingConfig::off)
        );
    }

    public static QuerqyConfig empty() {
        return QuerqyConfig.builder().build();
    }

    public static class QuerqyConfigBuilder {
        public QuerqyConfig.QuerqyConfigBuilder commonRules(final CommonRulesDefinition commonRulesDefinition) {
            final RewriterFactory rewriterFactory = CommonRulesFactoryBuilder.of(commonRulesDefinition).build();
            return this.rewriterFactory(rewriterFactory);
        }

        public QuerqyConfig.QuerqyConfigBuilder replaceRules(final ReplaceRulesDefinition replaceRulesDefinition) {
            final RewriterFactory rewriterFactory = ReplaceRulesFactoryBuilder.of(replaceRulesDefinition).build();
            return this.rewriterFactory(rewriterFactory);
        }
    }
}
