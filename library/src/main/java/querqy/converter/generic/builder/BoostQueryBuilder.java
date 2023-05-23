package querqy.converter.generic.builder;

import querqy.converter.generic.model.BoostQueryDefinition;

public interface BoostQueryBuilder<T> {

    @Deprecated T convertBoostUp(final BoostQueryDefinition<T> boostQueryDefinition);
    @Deprecated T convertBoostDown(final BoostQueryDefinition<T> boostQueryDefinition);

    T createAddToBoostParamQuery(final BoostQueryDefinition<T> boostQueryDefinition);
    T createMultiplyWithBoostParamQuery(final BoostQueryDefinition<T> boostQueryDefinition);

}
