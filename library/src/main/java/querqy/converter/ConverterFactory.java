package querqy.converter;

import querqy.model.ExpandedQuery;

public interface ConverterFactory<T> {

    Converter<T> createConverter(final ExpandedQuery expandedQuery);

}
