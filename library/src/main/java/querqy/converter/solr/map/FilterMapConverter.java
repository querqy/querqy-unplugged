package querqy.converter.solr.map;

import lombok.Builder;
import querqy.QueryConfig;
import querqy.model.QuerqyQuery;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public class FilterMapConverter {

    private final QueryConfig queryConfig;
    private final MapConverterConfig converterConfig;
    private final Collection<QuerqyQuery<?>> filterQueries;

    public boolean hasFilters() {
        if (filterQueries == null) {
            return false;

        } else {
            return filterQueries.size() > 0;
        }
    }

    public List<Object> convertFilterQueries() {
        if (filterQueries == null) {
            return List.of();

        } else {
            return convertNonNullFilterQueries();
        }
    }

    private List<Object> convertNonNullFilterQueries() {
        return filterQueries.stream()
                .map(node ->
                        QuerqyQueryMapConverter.builder()
                                .queryConfig(queryConfig)
                                .converterConfig(converterConfig)
                                .node(node)
                                .parseAsUserQuery(false)
                                .build()
                                .convert()
                )
                .collect(Collectors.toList());
    }
}
