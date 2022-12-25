package querqy.converter.solr.map;

import lombok.AccessLevel;
import lombok.Builder;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.converter.solr.map.boost.BoostConverter;
import querqy.converter.solr.map.boost.ConvertedBoostQueries;
import querqy.model.BoostQuery;
import querqy.model.ExpandedQuery;
import querqy.model.QuerqyQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Builder(access = AccessLevel.PACKAGE)
public class MapConverter implements Converter<Map<String, Object>> {

    @Deprecated private final QueryConfig queryConfig;
    @Deprecated private final MapConverterConfig converterConfig;

    private final QuerqyQueryConverter querqyQueryConverter;

    private final FilterConverter filterConverter;
    private final BoostConverter boostConverter;

    @Override
    public Map<String, Object> convert(final ExpandedQuery expandedQuery) {
        final Map<String, Object> convertedUserQuery = convertUserQuery(expandedQuery);

        final List<Object> filterQueries = parseFilterQueries(expandedQuery);
        final ConvertedBoostQueries boostMaps = parseBoostQueries(expandedQuery);

        return QueryConverter.builder()
                .userQuery(convertedUserQuery)
                .filterQueries(filterQueries)
                .boostQueries(boostMaps)
                .build()
                .expand();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertUserQuery(final ExpandedQuery expandedQuery) {
        final Object query =  querqyQueryConverter.convert(expandedQuery.getUserQuery());

        // TODO: Why is this needed?
        //  -> raw query not possible for user query
        //  -> match all query not identified by querqy core
        if (query instanceof Map) {
            return (Map<String, Object>) query;

        } else if (query instanceof String){
            return Map.of("bool", Map.of("must", (String) query));

        } else {
            throw new IllegalArgumentException("Converted user query must be of type Map or String");
        }
    }

    private List<Object> parseFilterQueries(final ExpandedQuery expandedQuery) {
        final Collection<QuerqyQuery<?>> filterQueries = expandedQuery.getFilterQueries();

        if (filterQueries != null && filterQueries.size() > 0) {
            return filterConverter.convertFilterQueries(filterQueries);

        } else {
            return List.of();
        }
    }

    private ConvertedBoostQueries parseBoostQueries(final ExpandedQuery expandedQuery) {
        final Collection<BoostQuery> boostUpQueries = getBoostUpQueries(expandedQuery);
        final Collection<BoostQuery> boostDownQueries = getBoostDownQueries(expandedQuery);

        if (boostUpQueries.isEmpty() && boostDownQueries.isEmpty()) {
            return ConvertedBoostQueries.empty();

        } else {
            return boostConverter.convertBoostQueries(boostUpQueries, boostDownQueries);
        }
    }

    private Collection<BoostQuery> getBoostUpQueries(final ExpandedQuery expandedQuery) {
        final Collection<BoostQuery> boostQueries = expandedQuery.getBoostUpQueries();
        return boostQueries == null ? List.of() : boostQueries;
    }

    private Collection<BoostQuery> getBoostDownQueries(final ExpandedQuery expandedQuery) {
        final Collection<BoostQuery> boostQueries = expandedQuery.getBoostDownQueries();
        return boostQueries == null ? List.of() : boostQueries;
    }

    @Builder
    private static class QueryConverter {
        private final Map<String, Object> userQuery;
        private final List<Object> filterQueries;
        private final ConvertedBoostQueries boostQueries;

        private final Stream.Builder<Map.Entry<String, Object>> queryNodeEntries = Stream.builder();
        private final Stream.Builder<Map.Entry<String, Object>> nestedQueryNodeEntries = Stream.builder();

        public Map<String, Object> expand() {
            if (filterQueries.isEmpty() && boostQueries.isEmpty()) {
                return Map.of("query", userQuery);

            } else {
                appendUserQuery();
                appendFilters();
                appendBoosts();

                queryNodeEntries.accept(Map.entry("query", createNestedQueryNode()));
                return createQueryNode();
            }
        }

        private void appendUserQuery() {
            nestedQueryNodeEntries.accept(Map.entry("must", userQuery));
        }

        private void appendFilters() {
            if (!filterQueries.isEmpty()) {
                queryNodeEntries.accept(Map.entry("filter", filterQueries));
            }
        }

        private void appendBoosts() {
            if (!boostQueries.isEmpty()) {
                nestedQueryNodeEntries.accept(Map.entry("should", boostQueries.getBoostFunctionQueries()));
                queryNodeEntries.accept(Map.entry("queries", boostQueries.getReferencedQueries()));

            }
        }

        private Map<String, Object> createQueryNode() {
            return createMap(queryNodeEntries);
        }

        private Map<String, Object> createNestedQueryNode() {
            return Map.of("bool", createMap(nestedQueryNodeEntries));
        }

        private Map<String, Object> createMap(final Stream.Builder<Map.Entry<String, Object>> builder) {
            return builder.build()
                    .collect(
                            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                    );
        }
    }
}
