package querqy.converter.generic;

import lombok.Builder;
import lombok.NonNull;
import querqy.BoostConfig;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;
import querqy.converter.generic.model.BooleanQueryDefinition;
import querqy.converter.generic.model.BoostQueryDefinition;
import querqy.model.BoostQuery;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
public class GenericBoostConverter<T> {

    @NonNull private final GenericQuerqyQueryConverter<T> genericQuerqyQueryConverter;
    @NonNull private final BoostQueryBuilder<T> boostQueryBuilder;

    @NonNull private final BooleanQueryBuilder<T> booleanQueryBuilder;
    @NonNull private final ConstantScoreQueryBuilder<T> constantScoreQueryBuilder;

    @NonNull private final BoostConfig boostConfig;

    public List<T> convert(final Collection<BoostQuery> boostUpQueries,
                           final Collection<BoostQuery> boostDownQueries) {
        return Stream
                .concat(
                        createBoostQueryDefinitions(boostUpQueries)
                                .map(this::convertBoostUpQuery),
                        createBoostQueryDefinitions(boostDownQueries)
                                .map(this::createBoostDownQuery)
                )
                .collect(Collectors.toList());
    }

    private Stream<BoostQueryDefinition<T>> createBoostQueryDefinitions(final Collection<BoostQuery> boostQueries) {
        if (boostQueries == null) {
            return Stream.empty();

        } else {
            return boostQueries.stream().map(this::createBoostQueryDefinition);
        }
    }

    private BoostQueryDefinition<T> createBoostQueryDefinition(final BoostQuery boostQuery) {
        return BoostQueryDefinition.<T>builder()
                .query(genericQuerqyQueryConverter.convert(boostQuery.getQuery()))
                .boostConfig(boostConfig)
                .boost(boostQuery.getBoost())
                .build();
    }

    private T convertBoostUpQuery(final BoostQueryDefinition<T> boostQueryDefinition) {
        final BoostConfig.QueryScoreConfig queryScoreConfig = boostConfig.getQueryScoreConfig();

        switch (queryScoreConfig) {
            case IGNORE: return createIgnoringScoreQuery(boostQueryDefinition);
            case ADD_TO_BOOST_PARAM: return boostQueryBuilder.convertBoostUp(boostQueryDefinition);
            case MULTIPLY_WITH_BOOST_PARAM: return boostQueryBuilder.convertBoostUp(boostQueryDefinition);

            default:
                throw new IllegalArgumentException(
                        "Boost mode " + queryScoreConfig + " is not supported by " + this.getClass().getName());
        }
    }

    private T createIgnoringScoreQuery(final BoostQueryDefinition<T> boostQueryDefinition) {
        return constantScoreQueryBuilder.build(boostQueryDefinition.getQuery(), boostQueryDefinition.getBoost());
    }

    private T createAddToBoostParamQuery(final BoostQueryDefinition<T> boostQueryDefinition) {
        return boostQueryBuilder.createAddToBoostParamQuery(boostQueryDefinition);
    }

    private T createMultiplyWithBoostParamQuery(final BoostQueryDefinition<T> boostQueryDefinition) {
        return boostQueryBuilder.createMultiplyWithBoostParamQuery(boostQueryDefinition);
    }

    private T createBoostDownQuery(final BoostQueryDefinition<T> boostQueryDefinition) {
        final T mustNotQuery = createMustNotQuery(boostQueryDefinition.getQuery());
        return constantScoreQueryBuilder.build(mustNotQuery, boostQueryDefinition.getBoost());
    }

    private T createMustNotQuery(final T query) {
        final BooleanQueryDefinition<T> mustNotQueryDefinition = BooleanQueryDefinition.<T>builder()
                .mustNotClause(query)
                .minimumShouldMatch("100%")
                .boost(1.0f)
                .build();

        return booleanQueryBuilder.build(mustNotQueryDefinition);
    }
}
