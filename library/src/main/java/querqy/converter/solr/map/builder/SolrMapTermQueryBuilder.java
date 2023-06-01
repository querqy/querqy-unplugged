package querqy.converter.solr.map.builder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import querqy.QueryTypeConfig;
import querqy.converter.generic.builder.TermQueryBuilder;
import querqy.converter.generic.model.TermQueryDefinition;

import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class SolrMapTermQueryBuilder implements TermQueryBuilder<Map<String, Object>> {


    @NonNull private final QueryTypeConfig defaultTermQueryTypeConfig;

    @Override
    public Map<String, Object> build(final TermQueryDefinition termQueryDefinition) {
        return createTermQuery(termQueryDefinition);

//        if (termQueryDefinition.isConstantScoreQuery()) {
//            return createTermQuery(termQueryDefinition);
//
//        } else {
//            throw new UnsupportedOperationException(
//                    this.getClass().getName() + " currently only supports creating constant score queries");
//        }
    }

//    private Map<String, Object> createConstantScoreQuery(final TermQueryDefinition termQueryDefinition) {
//        final Map<String, Object> termQuery = createTermQuery(termQueryDefinition);
//
//        final Map<String, Object> constantScoreQuery = Map.of(
//                "filter", termQuery,
//                "boost", termQueryDefinition.getTermBoost()
//        );
//
//        return Map.of(constantScoreQueryTypeName, constantScoreQuery);
//    }

    private Map<String, Object> createTermQuery(final TermQueryDefinition termQueryDefinition) {
        final QueryTypeConfig queryTypeConfig = termQueryDefinition.getFieldConfig().getQueryTypeConfig()
                .orElse(defaultTermQueryTypeConfig);

        return Map.of(
                queryTypeConfig.getTypeName(), Map.of(
                        queryTypeConfig.getQueryParamName(), termQueryDefinition.getTerm(),
                        queryTypeConfig.getFieldParamName(), termQueryDefinition.getFieldName()
                )
        );
    }
}
