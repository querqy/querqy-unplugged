package querqy.converter.generic.builder;

import querqy.converter.generic.model.BoostQueryDefinition;

public interface BoostQueryBuilder<T> {

    T createAddToBoostParamQuery(final BoostQueryDefinition<T> boostQueryDefinition);
    T createMultiplyWithBoostParamQuery(final BoostQueryDefinition<T> boostQueryDefinition);

    T createClassicBoostQuery(final BoostQueryDefinition<T> boostQueryDefinition);
}
