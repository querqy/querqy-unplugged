package support;

import lombok.NoArgsConstructor;
import querqy.converter.generic.builder.QueryStringQueryBuilder;

import java.util.Locale;

@NoArgsConstructor(staticName = "create")
public class StringQueryStringQueryBuilder implements QueryStringQueryBuilder<String> {
    @Override
    public String build(final String queryString) {
        return String.format(Locale.US, "converted(%s)", queryString);
    }
}
