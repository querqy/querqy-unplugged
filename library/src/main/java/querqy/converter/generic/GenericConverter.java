package querqy.converter.generic;

import lombok.Builder;
import lombok.NonNull;
import querqy.converter.Converter;
import querqy.converter.generic.model.ExpandedQueryDefinition;
import querqy.model.ExpandedQuery;
import querqy.model.QuerqyQuery;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public class GenericConverter<T> implements Converter<T> {

    @NonNull private final GenericExpandedQueryConverter<T> genericExpandedQueryConverter;
    @NonNull private final GenericQuerqyQueryConverter<T> genericQuerqyQueryConverter;
    @NonNull private final GenericBoostConverter<T> genericBoostConverter;

    @Override
    public T convert(final ExpandedQuery expandedQuery) {
        final T userQuery = convertUserQuery(expandedQuery.getUserQuery());
        final List<T> filterQueries = convertFilterQueries(expandedQuery.getFilterQueries());
        final List<T> boostQueries = genericBoostConverter.convert(
                expandedQuery.getBoostUpQueries(), expandedQuery.getBoostDownQueries()
        );

        final ExpandedQueryDefinition<T> expandedQueryDefinition = ExpandedQueryDefinition.<T>builder()
                .userQuery(userQuery)
                .filterQueries(filterQueries)
                .boostQueries(boostQueries)
                .build();


        return genericExpandedQueryConverter.convert(expandedQueryDefinition);
    }

    private T convertUserQuery(final QuerqyQuery<?> userQuery) {
        return genericQuerqyQueryConverter.convert(userQuery);
    }

    // TODO: Put into GenericFilterConverter
    private List<T> convertFilterQueries(final Collection<QuerqyQuery<?>> filterQueries) {
        if (filterQueries == null) {
            return List.of();

        } else {
            return convertNonNullFilterQueries(filterQueries);
        }
    }

    private List<T> convertNonNullFilterQueries(final Collection<QuerqyQuery<?>> filterQueries) {
        return filterQueries.stream()
                .map(genericQuerqyQueryConverter::convert)
                .collect(Collectors.toList());
    }

}
