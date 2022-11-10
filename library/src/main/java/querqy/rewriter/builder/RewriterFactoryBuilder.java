package querqy.rewriter.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import querqy.rewrite.RewriterFactory;
import querqy.rewrite.commonrules.SimpleCommonRulesRewriterFactory;
import querqy.rewrite.contrib.ReplaceRewriterFactory;

import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class RewriterFactoryBuilder {

    private final RewriterType type;
    private final Map<String, String> attributes;

    final ObjectMapper mapper = new ObjectMapper();

    public RewriterFactory build() {
        switch (type) {
            case COMMON_RULES: return createCommonRulesFactory();
            case REPLACE_RULES: return createReplaceRulesFactory();

            default:
                throw new UnsupportedOperationException(
                        "Rewriter type \"" + type.getName() + "\" is not supported by RewriterSupport");
        }

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

}
