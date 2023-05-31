package support;

import lombok.NoArgsConstructor;
import querqy.converter.generic.builder.WrappedQueryBuilder;

import java.util.Locale;

@NoArgsConstructor(staticName = "create")
public class StringWrappedQueryBuilder implements WrappedQueryBuilder<String> {

    @Override
    public String wrap(String query) {
        return String.format(Locale.US, "wrapped(%s)", query);
    }
}
