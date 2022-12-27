package querqy.converter.solr.map;

import java.util.Arrays;
import java.util.Map;

public class MapConverterTestUtils {

    public static Map<String, Object> bqMapSingleMust(final Object clause) {
        return Map.of(
                "bool",
                Map.of("must", clause));
    }

    public static Map<String, Object> constantScoreTermMap(final Map<String, Object> termMap, final Float weight) {
        return Map.of(
                "constantScore", Map.of(
                        "filter", termMap,
                        "boost", weight
                )
        );
    }

    public static Map<String, Object> constantScoreTermMap(final String field, final String value, final Float weight) {
        return constantScoreTermMap(
                Map.of(
                        "field", Map.of(
                                "query", value,
                                "f", field)
                ),
                weight
        );
    }

}
