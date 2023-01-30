package querqy.converter.generic.builder;

import querqy.converter.generic.model.BoostQueryDefinition;

public interface BoostQueryBuilder<T> {

    T convertBoostUp(final BoostQueryDefinition<T> boostQueryDefinition);
    T convertBoostDown(final BoostQueryDefinition<T> boostQueryDefinition);

}
