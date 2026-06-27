package querqy.rewriter.builder;

import lombok.RequiredArgsConstructor;
import querqy.rewriter.regexreplace.RegexReplaceRewriterFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor(staticName = "of")
public class RegexReplaceRulesFactoryBuilder {

    private final RegexReplaceRulesDefinition definition;

    public RegexReplaceRewriterFactory build() {

        try (
                final ByteArrayInputStream rulesInput = new ByteArrayInputStream(
                        definition.getRules().getBytes(StandardCharsets.UTF_8));
                final InputStreamReader rulesReader = new InputStreamReader(rulesInput, StandardCharsets.UTF_8)
        ){
            return new RegexReplaceRewriterFactory(
                    definition.getRewriterId(),
                    rulesReader,
                    definition.isIgnoreCase()
            );
        } catch (IOException e) {
            throw new RewriterFactoryBuilderException(e);
        }

    }

}
