package querqy.converter.generic;

import lombok.Builder;
import querqy.converter.Converter;
import querqy.model.ExpandedQuery;
import querqy.model.QuerqyQuery;
import querqy.model.Query;

@Builder
public class GenericConverter<T> implements Converter<T> {

    private final GenericQuerqyQueryConverter<T> genericQuerqyQueryConverter;

    @Override
    public T convert(final ExpandedQuery expandedQuery) {
        final QuerqyQuery<?> userQuery = expandedQuery.getUserQuery();

        if (userQuery instanceof Query) {
            return genericQuerqyQueryConverter.visit((Query) userQuery);

        } else {
            throw new UnsupportedOperationException("TODO");
        }
    }
}
