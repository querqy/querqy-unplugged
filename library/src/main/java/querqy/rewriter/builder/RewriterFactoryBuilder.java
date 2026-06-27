package querqy.rewriter.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import querqy.rewrite.RewriterFactory;
import querqy.rewriter.commonrules.SimpleCommonRulesRewriterFactory;
import querqy.rewriter.PhraseBoostRewriterFactory;
import querqy.rewriter.replace.ReplaceRewriterFactory;
import querqy.rewriter.regexreplace.RegexReplaceRewriterFactory;

import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class RewriterFactoryBuilder {

    private final RewriterType type;
    private final Map<String, String> attributes;

    final ObjectMapper mapper = new ObjectMapper();

    public RewriterFactory build() {
        return switch (type) {
            case COMMON_RULES -> createCommonRulesFactory();
            case REPLACE_RULES -> createReplaceRulesFactory();
            case REGEX_REPLACE_RULES -> createRegexReplaceRulesFactory();
            case PHRASE_BOOST -> createPhraseBoostFactory();
        };

    }

    private SimpleCommonRulesRewriterFactory createCommonRulesFactory() {
        final CommonRulesDefinition definition = mapper.convertValue(attributes, CommonRulesDefinition.class);
        return CommonRulesFactoryBuilder.of(definition)
                .build();
    }

    private ReplaceRewriterFactory createReplaceRulesFactory() {
        final ReplaceRulesDefinition definition = mapper.convertValue(attributes, ReplaceRulesDefinition.class);
        return ReplaceRulesFactoryBuilder.of(definition)
                .build();
    }

    private RegexReplaceRewriterFactory createRegexReplaceRulesFactory() {
        final RegexReplaceRulesDefinition definition = mapper.convertValue(attributes, RegexReplaceRulesDefinition.class);
        return RegexReplaceRulesFactoryBuilder.of(definition)
                .build();
    }

    private PhraseBoostRewriterFactory createPhraseBoostFactory() {
        final PhraseBoostDefinition definition = mapper.convertValue(attributes, PhraseBoostDefinition.class);
        return PhraseBoostFactoryBuilder.of(definition)
                .build();
    }

}
