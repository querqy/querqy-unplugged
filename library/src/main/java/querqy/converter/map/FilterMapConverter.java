package querqy.converter.map;

import lombok.Builder;
import lombok.Singular;
import querqy.QueryConfig;
import querqy.model.QuerqyQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public class FilterMapConverter {

    private final QueryConfig queryConfig;

    // TODO: could be null
    private final Collection<QuerqyQuery<?>> filterQueries;

    // TODO: boosts in own converter
//    private final Collection<BoostQuery> boostUpQueries;
//    private final Collection<BoostQuery> boostDownQueries;

//    @Builder
//    public static FilterAndBoostMapConverter build(final QueryConfig queryConfig, final ExpandedQuery expandedQuery) {
//
//        final Collection<QuerqyQuery<?>> filterQueries = expandedQuery.getFilterQueries() == null ?
//                List.of() : expandedQuery.getFilterQueries();
//
//        final Collection<BoostQuery> boostUpQueries = expandedQuery.getBoostUpQueries() == null ?
//                List.of() : expandedQuery.getBoostUpQueries();
//
//        final Collection<BoostQuery> boostDownQueries = expandedQuery.getBoostDownQueries() == null ?
//                List.of() : expandedQuery.getBoostDownQueries();
//
//        return FilterAndBoostMapConverter.of(queryConfig, filterQueries, boostUpQueries, boostDownQueries);
//    }

    public boolean hasFilters() {
        if (filterQueries == null) {
            return false;

        } else {
            return filterQueries.size() > 0;
        }
    }

//    public boolean hasBoosts() {
//        return boostUpQueries.size() > 0 || boostDownQueries.size() > 0;
//    }

    public List<Map<String, Object>> convertFilterQueries() {
        if (filterQueries == null) {
            return List.of();

        } else {
            return convertNonNullFilterQueries();
        }
    }

    private List<Map<String, Object>> convertNonNullFilterQueries() {
        return filterQueries.stream()
                .map(filterQuery -> QuerqyQueryMapConverter.builder()
                        .queryConfig(queryConfig)
                        .node(filterQuery)
                        .parseAsUserQuery(false)
                        .build()
                        .convert())
                .collect(Collectors.toList());
    }

//    public List<Map<String, Object>> convertBoostQueries() {
//        throw new UnsupportedOperationException("Not implemented so far");
//    }
//
//    private Map<String, Object> convertBoostQuery(final BoostQuery boostQuery) {
//        throw new UnsupportedOperationException("Not implemented so far");
//    }
}
