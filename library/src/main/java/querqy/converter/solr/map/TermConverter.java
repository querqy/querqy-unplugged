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
public class TermConverter {

    private final QueryConfig queryConfig;
    @Deprecated private final MapConverterConfig converterConfig;

    public List<Object> createTermQueries(final Term term) {
        final float termBoost = getTermBoost(term);

        return getWeightedFieldStream()
                .map(
                        field -> createTermQuery(field.getKey(), field.getValue() * termBoost, term)
                )
                .collect(Collectors.toList());
    }

    private Stream<Map.Entry<String, Float>> getWeightedFieldStream() {
        return queryConfig.getFields().entrySet().stream();
    }

    private float getTermBoost(final Term term) {
        if (term instanceof BoostedTerm) {
            return ((BoostedTerm) term).getBoost();

        } else {
            return 1f;
        }
    }

    // TODO: reduce parameters
    private Map<String, Object> createTermQuery(final String field, final float weight, final Term term) {
        return Map.of(
                converterConfig.getScoringNodeName(),
                Map.of(
                        "filter", createMatchingNode(field, term),
                        "boost", weight
                )
        );
    }


    private Map<String, Object> createMatchingNode(final String field, final Term term) {
        return Map.of(
                converterConfig.getMatchingNodeName(),
                Map.of(
                        "f", field,
                        "query", term.getValue().toString()
                )
        );
    }
}
