package querqy.converter.solr.map.builder;

import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.DismaxQueryBuilder;
import querqy.converter.generic.model.DismaxQueryDefinition;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public class SolrMapDismaxQueryBuilder implements DismaxQueryBuilder<Map<String, Object>> {

    // TODO: queryTypeName

    @Override
    public Map<String, Object> build(DismaxQueryDefinition<Map<String, Object>> dismaxQueryDefinition) {
        final Map<String, Object> innerNode = new HashMap<>(2);

        dismaxQueryDefinition.getTie().ifPresent(tie -> innerNode.put("tie", tie));
        innerNode.put("queries", dismaxQueryDefinition.getDismaxClauses());

        return Map.of("nestedDismax", innerNode);
    }
}
