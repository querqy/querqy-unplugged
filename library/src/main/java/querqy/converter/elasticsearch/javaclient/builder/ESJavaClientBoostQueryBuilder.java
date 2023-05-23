package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.converter.generic.model.BoostQueryDefinition;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientBoostQueryBuilder implements BoostQueryBuilder<Query> {

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
