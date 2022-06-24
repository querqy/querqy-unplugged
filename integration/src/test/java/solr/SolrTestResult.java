package solr;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SolrTestResult extends ArrayList<Map<String, Object>> {

    public SolrTestResult print() {
        System.out.println("------------------------------------------");

        for (final Map<String, Object> doc : this) {
            System.out.println(doc);
        }

        System.out.println("------------------------------------------");
        System.out.println(this.hashCode());

        return this;
    }

    public static SolrTestResult.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final LinkedHashMap<String, List<Object>> docs = new LinkedHashMap<>();
        private int docCount = 0;

        public SolrTestResult.Builder fields(final String... fields) {
            Arrays.stream(fields).forEach(field -> docs.put(field, new ArrayList<>()));
            return this;
        }

        public SolrTestResult.Builder doc(final Object... values) {
            assert docs.size() > 0 : "Fields must be set before adding a doc";
            assert docs.size() == values.length : "Number of doc values must match the number of fields";

            int valueIndex = 0;
            for (final List<Object> field : docs.values()) {
                field.add(values[valueIndex]);
                valueIndex++;
            }

            docCount++;
            return this;
        }

        public SolrTestResult build() {
            return IntStream.range(0, docCount)
                    .mapToObj(this::createDocForIndex)
                    .collect(Collectors.toCollection(SolrTestResult::new));
        }

        private Map<String, Object> createDocForIndex(final int index) {
            return docs.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().get(index)
                    ));
        }
    }
}
