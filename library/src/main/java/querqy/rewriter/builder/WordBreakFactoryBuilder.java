package querqy.rewriter.builder;

import lombok.RequiredArgsConstructor;
import querqy.rewriter.wordbreak.WordBreakCompoundRewriterFactory;

@RequiredArgsConstructor(staticName = "of")
public class WordBreakFactoryBuilder {

    private final WordBreakDefinition definition;

    public WordBreakCompoundRewriterFactory build() {
        return new WordBreakCompoundRewriterFactory(
                definition.getRewriterId(),
                definition.getTermCorpus(),
                definition.isLowerCaseInput(),
                definition.getMinSuggestionFreq(),
                definition.getMinBreakLength(),
                definition.getReverseCompoundTriggerWords(),
                definition.isAlwaysAddReverseCompounds(),
                definition.getMaxDecompoundExpansions(),
                definition.isVerifyDecompoundCollation(),
                definition.getProtectedWords(),
                definition.getDecompoundMorphology(),
                definition.getCompoundMorphology()
        );
    }

}
