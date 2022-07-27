package querqy.converter.solr.map;

import lombok.Builder;
import querqy.QueryConfig;
import querqy.model.BoostedTerm;
import querqy.model.Term;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
public class TermMapConverter {

    private final QueryConfig queryConfig;
    private final Term term;

    public List<Object> createTermQueries() {
        final float termBoost = getTermBoost();

        return getWeightedFieldStream()
                .map(
                        field -> createTermQuery(field.getKey(), field.getValue() * termBoost)
                )
                .collect(Collectors.toList());
    }

    private Stream<Map.Entry<String, Float>> getWeightedFieldStream() {
        return queryConfig.getFields().entrySet().stream();
    }

    private float getTermBoost() {
        if (term instanceof BoostedTerm) {
            return ((BoostedTerm) term).getBoost();

        } else {
            return 1f;
        }
    }

    private Map<String, Object> createTermQuery(final String field, final float weight) {
        return Map.of(
                queryConfig.getScoringNodeName(),
                Map.of(
                        "filter", createMatchingNode(field),
                        "boost", weight
                )
        );
    }


    private Map<String, Object> createMatchingNode(final String field) {
        return Map.of(
                queryConfig.getMatchingNodeName(),
                Map.of(
                        "f", field,
                        "query", term.getValue().toString()
                )
        );
    }
}
