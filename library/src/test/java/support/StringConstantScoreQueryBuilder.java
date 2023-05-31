package support;

import lombok.NoArgsConstructor;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;

import java.util.Locale;

@NoArgsConstructor(staticName = "create")
public class StringConstantScoreQueryBuilder implements ConstantScoreQueryBuilder<String> {

    @Override
    public String build(final String query, final float constantScore) {
        return String.format(Locale.US, "constant(%s,%f)", query, constantScore);
    }
}
