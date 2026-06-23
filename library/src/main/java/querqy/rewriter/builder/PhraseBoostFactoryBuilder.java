package querqy.rewriter.builder;

import lombok.RequiredArgsConstructor;
import querqy.rewrite.contrib.PhraseBoostRewriter.FieldAndBoost;
import querqy.rewrite.contrib.PhraseBoostRewriter.PhraseTypeConfig;
import querqy.rewrite.contrib.PhraseBoostRewriterFactory;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "of")
public class PhraseBoostFactoryBuilder {

    private final PhraseBoostDefinition definition;

    public PhraseBoostRewriterFactory build() {
        return new PhraseBoostRewriterFactory(
                definition.getRewriterId(),
                toConfig(definition.getBigram()),
                toConfig(definition.getTrigram()),
                toConfig(definition.getFull()),
                definition.getTieBreaker()
        );
    }

    private static PhraseTypeConfig toConfig(final PhraseConfig phraseConfig) {
        if (phraseConfig == null || phraseConfig.getFields() == null || phraseConfig.getFields().isEmpty()) {
            return null;
        }
        final List<FieldAndBoost> fields = phraseConfig.getFields().stream()
                .map(fb -> new FieldAndBoost(fb.getField(), fb.getBoost()))
                .collect(Collectors.toList());
        return new PhraseTypeConfig(fields, phraseConfig.getSlop());
    }
}
