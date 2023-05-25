package querqy.converter.solr.map.builder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import querqy.BoostConfig;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.converter.generic.model.BoostQueryDefinition;

import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public class SolrMapBoostQueryBuilder implements BoostQueryBuilder<Map<String, Object>> {

    private static final String ADD_TO_BOOST_PARAM_TEMPLATE = "if(query($%s),sum(%f,query($%s)),0)";
    private static final String MULTIPLY_WITH_BOOST_PARAM_TEMPLATE = "mul(%f,query($%s))";
    private static final String CLASSIC_BOOST_TEMPLATE = "mul(%f,sub(1,div(1,sum(query($%s),1))))";

    @NonNull private final SolrMapQueryReferenceBuilder queryReferenceBuilder;

    @Override
    public Map<String, Object> createAddToBoostParamQuery(final BoostQueryDefinition<Map<String, Object>> boostQueryDefinition) {
        final String queryReference = queryReferenceBuilder.createReferenceForQuery(boostQueryDefinition.getQuery());
        final String function = String.format(
                Locale.US, ADD_TO_BOOST_PARAM_TEMPLATE, queryReference, boostQueryDefinition.getBoost(), queryReference);
        return createFunctionQuery(function);
    }

    @Override
    public Map<String, Object> createMultiplyWithBoostParamQuery(final BoostQueryDefinition<Map<String, Object>> boostQueryDefinition) {
        final String queryReference = queryReferenceBuilder.createReferenceForQuery(boostQueryDefinition.getQuery());
        final String function = String.format(
                Locale.US, MULTIPLY_WITH_BOOST_PARAM_TEMPLATE, boostQueryDefinition.getBoost(), queryReference);
        return createFunctionQuery(function);
    }

    @Override
    public Map<String, Object> createClassicBoostQuery(final BoostQueryDefinition<Map<String, Object>> boostQueryDefinition) {
        final String queryReference = queryReferenceBuilder.createReferenceForQuery(boostQueryDefinition.getQuery());

        if (boostQueryDefinition.getBoost() == 1) {
            return Map.of("param", queryReference);

        } else {
            final String function = String.format(
                    Locale.US, CLASSIC_BOOST_TEMPLATE, boostQueryDefinition.getBoost(), queryReference);
            return createFunctionQuery(function);
        }
    }

    private Map<String, Object> createFunctionQuery(final String function) {
        return Map.of(
                "func", Map.of("v", function)
        );
    }
}
