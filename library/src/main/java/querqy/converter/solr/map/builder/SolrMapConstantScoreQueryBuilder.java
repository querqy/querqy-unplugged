package querqy.converter.solr.map.builder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;

import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class SolrMapConstantScoreQueryBuilder implements ConstantScoreQueryBuilder<Map<String, Object>> {

    @NonNull
    private final String constantScoreQueryTypeName;

    @Override
    public Map<String, Object> build(final Map<String, Object> query, final float constantScore) {
        return Map.of(
                constantScoreQueryTypeName, Map.of(
                        "filter", query,
                        "boost", constantScore
                )
        );
    }
}
