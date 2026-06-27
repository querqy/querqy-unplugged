package querqy.rewriter.builder;

import org.junit.Test;
import querqy.QuerqyConfig;
import querqy.rewriter.wordbreak.TsvDfCoocTermCorpus;
import querqy.rewriter.wordbreak.TsvDfTermCorpus;
import querqy.rewriter.wordbreak.WordBreakCompoundRewriterFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WordBreakFactoryBuilderTest {

    @Test
    public void testThat_wordBreakRewriterIsCreatedProperly_withTsvDfTermCorpus() throws IOException {
        final TsvDfTermCorpus corpus = TsvDfTermCorpus.builder()
                .reader(new StringReader("apple\t100\nphone\t200\niphone\t50\n"))
                .build();

        final WordBreakDefinition definition = WordBreakDefinition.builder()
                .rewriterId("word-break")
                .termCorpus(corpus)
                .build();

        assertThat(WordBreakFactoryBuilder.of(definition).build())
                .isInstanceOf(WordBreakCompoundRewriterFactory.class);
    }

    @Test
    public void testThat_wordBreakRewriterIsCreatedProperly_withTsvDfCoocTermCorpus() throws IOException {
        final TsvDfCoocTermCorpus corpus = TsvDfCoocTermCorpus.builder()
                .reader(new StringReader("iphone\t50\t0000000000000000\n"))
                .hashFunctions(1)
                .build();

        final WordBreakDefinition definition = WordBreakDefinition.builder()
                .rewriterId("word-break")
                .termCorpus(corpus)
                .verifyDecompoundCollation(true)
                .build();

        assertThat(WordBreakFactoryBuilder.of(definition).build())
                .isInstanceOf(WordBreakCompoundRewriterFactory.class);
    }

    @Test
    public void testThat_wordBreakRewriterIsCreatedProperly_withAllOptions() throws IOException {
        final TsvDfTermCorpus corpus = TsvDfTermCorpus.builder()
                .reader(new StringReader("apple\t100\n"))
                .numDocs(1000)
                .build();

        final WordBreakDefinition definition = WordBreakDefinition.builder()
                .rewriterId("word-break")
                .termCorpus(corpus)
                .lowerCaseInput(true)
                .minSuggestionFreq(2)
                .minBreakLength(4)
                .reverseCompoundTriggerWords(List.of("for"))
                .alwaysAddReverseCompounds(true)
                .maxDecompoundExpansions(5)
                .protectedWords(List.of("apple"))
                .build();

        assertThat(WordBreakFactoryBuilder.of(definition).build())
                .isInstanceOf(WordBreakCompoundRewriterFactory.class);
    }

    @Test
    public void testThat_wordBreakRewriterCreationFails_whenCollationRequestedWithoutCoocCorpus() throws IOException {
        final TsvDfTermCorpus corpus = TsvDfTermCorpus.builder()
                .reader(new StringReader("apple\t100\n"))
                .build();

        final WordBreakDefinition definition = WordBreakDefinition.builder()
                .rewriterId("word-break")
                .termCorpus(corpus)
                .verifyDecompoundCollation(true)
                .build();

        assertThatThrownBy(() -> WordBreakFactoryBuilder.of(definition).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testThat_wordBreakRewriterIsAvailableViaQuerqyConfigBuilder() throws IOException {
        final TsvDfTermCorpus corpus = TsvDfTermCorpus.builder()
                .reader(new StringReader("apple\t100\n"))
                .build();

        final WordBreakDefinition definition = WordBreakDefinition.builder()
                .rewriterId("word-break")
                .termCorpus(corpus)
                .build();

        final QuerqyConfig config = QuerqyConfig.builder()
                .wordBreak(definition)
                .build();

        assertThat(config.getRewriteChain().getFactories()).hasSize(1);
    }
}
