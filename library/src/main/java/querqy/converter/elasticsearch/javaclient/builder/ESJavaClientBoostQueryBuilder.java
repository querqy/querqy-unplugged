package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ConstantScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.BoostConfig;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.converter.generic.model.BoostQueryDefinition;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientBoostQueryBuilder implements BoostQueryBuilder<Query> {

    @Override
    public Query convertBoostUp(final BoostQueryDefinition<Query> boostQueryDefinition) {
        return createBoostQuery(boostQueryDefinition);
    }

    @Override
    public Query convertBoostDown(final BoostQueryDefinition<Query> boostQueryDefinition) {
        return new Query(
                new BoolQuery.Builder()
                        .should(createMatchAllQuery())
                        .mustNot(createBoostQuery(boostQueryDefinition))
                        .build()
        );
    }

    private Query createBoostQuery(final BoostQueryDefinition<Query> boostQueryDefinition) {
        final BoostConfig.BoostMode boostMode = boostQueryDefinition.getBoostConfig().getBoostMode();

        switch (boostMode) {
            case BOOST_SCORE_ONLY: return createConstantScoreQuery(boostQueryDefinition);
            case ADDITIVE: return createAdditiveScoreQuery(boostQueryDefinition);
            case MULTIPLICATIVE: return createMultiplicativeScoreQuery(boostQueryDefinition);

            default:
                throw new IllegalArgumentException(
                        "Boost mode " + boostMode + " is not supported by " + this.getClass().getName());
        }
    }

    private Query createConstantScoreQuery(final BoostQueryDefinition<Query> boostQueryDefinition) {
        return new Query(
                new ConstantScoreQuery.Builder()
                        .filter(boostQueryDefinition.getQuery())
                        .boost(boostQueryDefinition.getBoost())
                        .build()
        );
    }

    private Query createAdditiveScoreQuery(final BoostQueryDefinition<Query> boostQueryDefinition) {
        return new Query(
                new FunctionScoreQuery.Builder()
                        .query(boostQueryDefinition.getQuery())
                        .functions(createFunctionScoreByWeight(boostQueryDefinition.getBoost()))
                        .boostMode(FunctionBoostMode.Sum)
                        .build()
        );
    }

    private Query createMultiplicativeScoreQuery(final BoostQueryDefinition<Query> boostQueryDefinition) {
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
