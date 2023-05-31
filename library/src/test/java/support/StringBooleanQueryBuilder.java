package support;

import lombok.NoArgsConstructor;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.model.BooleanQueryDefinition;

import java.util.List;

import static support.BuilderSupport.wrap;

@NoArgsConstructor(staticName = "create")
public class StringBooleanQueryBuilder implements BooleanQueryBuilder<String> {

    @Override
    public String build(final BooleanQueryDefinition<String> booleanQueryDefinition) {

        final String must = wrap("must", booleanQueryDefinition.getMustClauses());
        final String should = wrap("should", booleanQueryDefinition.getShouldClauses());
        final String filter = wrap("filter", booleanQueryDefinition.getFilterClauses());
        final String mustNot = wrap("mustNot", booleanQueryDefinition.getMustNotClauses());

        return wrap("bool", List.of(must, should, filter, mustNot));
    }
}
