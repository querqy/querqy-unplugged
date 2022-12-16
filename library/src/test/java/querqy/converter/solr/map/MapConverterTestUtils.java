package querqy.converter.solr.map;

import java.util.Arrays;
import java.util.Map;

public class MapConverterTestUtils {

    public static Map<String, Object> bqMap(final String occur, final Object... clauses) {
        return Map.of(
                "bool",
                Map.of(occur, Arrays.asList(clauses)));
    }

    public static Map<String, Object> bqMap(final String occur, final String mm, final Object... clauses) {
        return Map.of(
                "bool",
                Map.of(
                        occur, Arrays.asList(clauses),
                        "mm", mm
                ));
    }

    public static Map<String, Object> bqMap(final float boost, final String occur, final Object... clauses) {
        return Map.of(
                "bool",
                Map.of(
                        occur, Arrays.asList(clauses),
                        "boost", boost
                ));
    }

    public static Map<String, Object> dmqMap(final Float tie, final Object... clauses) {
        return Map.of(
                "nestedDismax",
                Map.of(
                        "queries", Arrays.asList(clauses),
                        "tie", tie
                )
        );
    }

    public static Map<String, Object> dmqMap(final Object... clauses) {
        return Map.of(
                "nestedDismax",
                Map.of("queries", Arrays.asList(clauses)));
    }

    public static Map<String, Object> termMap(final String field, final String value, final Float weight) {
        return Map.of(
                "constantScore", Map.of(
                        "filter", Map.of(
                                "field", Map.of(
                                        "query", value, "f", field
                                )
                        ),
                        "boost", weight
                )
        );
    }

}
