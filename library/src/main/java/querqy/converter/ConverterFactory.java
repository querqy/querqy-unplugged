package querqy.converter;

import querqy.QueryConfig;
import querqy.QueryExpansionConfig;

public interface ConverterFactory<T> {

    Converter<T> createConverter(final QueryConfig queryConfig);

    default Converter<T> createConverter(final QueryConfig queryConfig, final QueryExpansionConfig<T> queryExpansionConfig) {
        return createConverter(queryConfig);
    }

}
