package querqy.converter;

import querqy.model.ExpandedQuery;

public interface Converter<T> {

    T convert(final ExpandedQuery expandedQuery);

}
