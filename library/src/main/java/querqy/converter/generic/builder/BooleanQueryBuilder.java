package querqy.converter.generic.builder;

import querqy.converter.generic.model.BooleanQueryDefinition;

public interface BooleanQueryBuilder<T> {

    T build(final BooleanQueryDefinition<T> booleanQueryDefinition);

}
