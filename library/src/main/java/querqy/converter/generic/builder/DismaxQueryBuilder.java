package querqy.converter.generic.builder;

import querqy.converter.generic.model.DismaxQueryDefinition;

public interface DismaxQueryBuilder<T> {

    T build(final DismaxQueryDefinition<T> dismaxQueryDefinition);

}
