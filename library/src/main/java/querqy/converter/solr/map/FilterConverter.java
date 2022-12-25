package querqy.converter.solr.map;

import lombok.Builder;
import querqy.model.QuerqyQuery;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public class FilterConverter {

    private final QuerqyQueryConverter querqyQueryConverter;

    public List<Object> convertFilterQueries(final Collection<QuerqyQuery<?>> filterQueries) {
        if (filterQueries == null) {
            return List.of();

        } else {
            return convertNonNullFilterQueries(filterQueries);
        }
    }

    private List<Object> convertNonNullFilterQueries(final Collection<QuerqyQuery<?>> filterQueries) {
        return filterQueries.stream()
                .map(querqyQueryConverter::convert)
                .collect(Collectors.toList());
    }
}
