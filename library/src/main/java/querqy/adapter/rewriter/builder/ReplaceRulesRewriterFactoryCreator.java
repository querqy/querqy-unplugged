package querqy.adapter.rewriter.builder;

import lombok.Builder;
import lombok.NonNull;
import querqy.rewrite.commonrules.QuerqyParserFactory;
import querqy.rewrite.commonrules.WhiteSpaceQuerqyParserFactory;
import querqy.rewrite.contrib.ReplaceRewriterFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Builder(buildMethodName = "_create", builderClassName = "Creator", builderMethodName = "creator")
public class ReplaceRulesRewriterFactoryCreator {

    @NonNull private String rewriterId;
    @NonNull private String rules;

    @Builder.Default private boolean ignoreCase = true;
    @Builder.Default private String inputDelimiter = "\t";
    @Builder.Default private QuerqyParserFactory querqyParserFactory = new WhiteSpaceQuerqyParserFactory();

    public static class Creator {

        public ReplaceRewriterFactory createFactory() throws IOException {
            final ReplaceRulesRewriterFactoryCreator creator = this._create();

            return new ReplaceRewriterFactory(
                    creator.rewriterId,
                    new InputStreamReader(
                            new ByteArrayInputStream(creator.rules.getBytes(StandardCharsets.UTF_8))),
                    creator.ignoreCase,
                    creator.inputDelimiter,
                    creator.querqyParserFactory.createParser()
            );
        }
    }
}
