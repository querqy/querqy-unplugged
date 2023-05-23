package querqy.converter.generic.builder;

import querqy.converter.generic.model.BoostQueryDefinition;

public interface BoostQueryBuilder<T> {

    @Deprecated default T convertBoostUp(final BoostQueryDefinition<T> boostQueryDefinition) {
        return null;
    }

    @Deprecated default T convertBoostDown(final BoostQueryDefinition<T> boostQueryDefinition) {
        return null;
    }

    T createAddToBoostParamQuery(final BoostQueryDefinition<T> boostQueryDefinition);
    T createMultiplyWithBoostParamQuery(final BoostQueryDefinition<T> boostQueryDefinition);

}
