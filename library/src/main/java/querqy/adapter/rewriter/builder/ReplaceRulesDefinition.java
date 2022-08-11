package querqy.adapter.rewriter.builder;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import querqy.rewrite.commonrules.QuerqyParserFactory;
import querqy.rewrite.commonrules.WhiteSpaceQuerqyParserFactory;
import querqy.rewrite.commonrules.model.BoostInstruction;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"querqyParserFactory"})
@ToString
public class ReplaceRulesDefinition {

    @JsonAlias("id") @NonNull private String rewriterId;
    @NonNull private String rules;

    @Builder.Default private boolean ignoreCase = true;
    @Builder.Default private String inputDelimiter = "\t";
    @Builder.Default private QuerqyParserFactory querqyParserFactory = new WhiteSpaceQuerqyParserFactory();

}
