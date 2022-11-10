package querqy.rewriter.builder;

import lombok.RequiredArgsConstructor;
import querqy.rewrite.contrib.ReplaceRewriterFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor(staticName = "of")
public class ReplaceRulesFactoryBuilder {

    private final ReplaceRulesDefinition definition;

    public ReplaceRewriterFactory build() {

        try (
                final ByteArrayInputStream rulesInput = new ByteArrayInputStream(
                        definition.getRules().getBytes(StandardCharsets.UTF_8));
                final InputStreamReader rulesReader = new InputStreamReader(rulesInput)
        ){
            return new ReplaceRewriterFactory(
                    definition.getRewriterId(),
                    rulesReader,
                    definition.isIgnoreCase(),
                    definition.getInputDelimiter(),
                    definition.getQuerqyParserFactory().createParser()
            );
        } catch (IOException e) {
            throw new RewriterFactoryBuilderException(e);
        }

    }

}
