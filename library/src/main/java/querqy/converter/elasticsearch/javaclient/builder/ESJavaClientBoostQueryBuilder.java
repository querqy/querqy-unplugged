package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ConstantScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.BoostConfig;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.converter.generic.model.BoostQueryDefinition;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientBoostQueryBuilder implements BoostQueryBuilder<Query> {

    @Override
    public Query convertBoostUp(final BoostQueryDefinition<Query> boostQueryDefinition) {
        return createBoostQuery(boostQueryDefinition);
    }

    @Override
    public Query convertBoostDown(final BoostQueryDefinition<Query> boostQueryDefinition) {
        return new Query(
                new ConstantScoreQuery.Builder()
                        .filter(
                                new Query(
                                        new BoolQuery.Builder()
                                                .should(createMatchAllQuery())
                                                .mustNot(createBoostQuery(boostQueryDefinition))
                                                .build()
                                )
                        )
                        .boost(boostQueryDefinition.getBoost())
                        .build()
        );
    }

    private Query createBoostQuery(final BoostQueryDefinition<Query> boostQueryDefinition) {
        final BoostConfig.QueryScoreConfig queryScoreConfig = boostQueryDefinition.getBoostConfig().getQueryScoreConfig();

        switch (queryScoreConfig) {
            case IGNORE: return createIgnoringScoreQuery(boostQueryDefinition);
            case ADD_TO_BOOST_PARAM: return createAddToBoostParamQuery(boostQueryDefinition);
            case MULTIPLY_WITH_BOOST_PARAM: return createMultiplyWithBoostParamQuery(boostQueryDefinition);

            default:
                throw new IllegalArgumentException(
                        "Boost mode " + queryScoreConfig + " is not supported by " + this.getClass().getName());
        }
    }

    private Query createIgnoringScoreQuery(final BoostQueryDefinition<Query> boostQueryDefinition) {
        return new Query(
                new ConstantScoreQuery.Builder()
                        .filter(boostQueryDefinition.getQuery())
                        .boost(boostQueryDefinition.getBoost())
                        .build()
        );
    }

    @Override
    public Query createAddToBoostParamQuery(final BoostQueryDefinition<Query> boostQueryDefinition) {
        return new Query(
                new FunctionScoreQuery.Builder()
                        .query(boostQueryDefinition.getQuery())
                        .functions(createFunctionScoreByWeight(boostQueryDefinition.getBoost()))
                        .boostMode(FunctionBoostMode.Sum)
                        .build()
        );
    }

    @Override
    public Query createMultiplyWithBoostParamQuery(final BoostQueryDefinition<Query> boostQueryDefinition) {
        return new Query(
                new FunctionScoreQuery.Builder()
                        .query(boostQueryDefinition.getQuery())
                        .functions(createFunctionScoreByWeight(boostQueryDefinition.getBoost()))
                        .boostMode(FunctionBoostMode.Multiply)
                        .build()
        );
    }

    private Query createMatchAllQuery() {
        return new Query(new MatchAllQuery.Builder().build());
    }

    private FunctionScore createFunctionScoreByWeight(final Float weight) {
        return new FunctionScore.Builder()
                .filter(createMatchAllQuery())
                .weight(weight.doubleValue())
                .build();
    }
}
