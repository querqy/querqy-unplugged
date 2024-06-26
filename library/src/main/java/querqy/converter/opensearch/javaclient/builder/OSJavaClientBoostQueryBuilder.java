package querqy.converter.opensearch.javaclient.builder;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch._types.query_dsl.FunctionBoostMode;
import org.opensearch.client.opensearch._types.query_dsl.FunctionScore;
import org.opensearch.client.opensearch._types.query_dsl.FunctionScoreQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.util.ObjectBuilder;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.converter.generic.model.BoostQueryDefinition;

@RequiredArgsConstructor(staticName = "create")
public class OSJavaClientBoostQueryBuilder implements BoostQueryBuilder<Query> {

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

//                        .functions(createFunctionScoreByWeight(boostQueryDefinition.getBoost()))
                        .functions(fn -> (ObjectBuilder<FunctionScore>) fn.filter(createMatchAllQuery()).weight(boostQueryDefinition.getBoost().doubleValue()))
                        .boostMode(FunctionBoostMode.Multiply)
                        .build()
        );
    }

    @Override
    public Query createClassicBoostQuery(BoostQueryDefinition<Query> boostQueryDefinition) {
        throw new UnsupportedOperationException("Boost mode CLASSIC is not supported by " + this.getClass().getName());
    }

    private Query createMatchAllQuery() {
        return new Query(new MatchAllQuery.Builder().build());
    }

    private FunctionScore createFunctionScoreByWeight(final Float weight) {

        return null;


        //        FunctionScore.Builder builder = new FunctionScore.Builder().;
//        builder.filter(createMatchAllQuery());
//
//        FunctionScore.of(x -> (ObjectBuilder<FunctionScore>) x.filter(createMatchAllQuery()).weight(weight.doubleValue())));

//                .ContainerBuilder()
//                .filter(createMatchAllQuery())
//                .weight(weight.doubleValue())
//                .build();
//        return new FunctionScore.Builder.ContainerBuilder()
//                .filter(createMatchAllQuery())
//                .weight(weight.doubleValue())
//                .build();
    }
}
