package querqy.converter;

import querqy.QueryConfig;

public interface ConverterFactory<T> {

    Converter<T> createConverter(final QueryConfig queryConfig);

}
