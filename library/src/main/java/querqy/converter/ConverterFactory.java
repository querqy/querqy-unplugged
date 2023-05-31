package querqy.converter;

import querqy.QueryConfig;
import querqy.QueryExpansionConfig;

public interface ConverterFactory<T> {

    @Deprecated // not needed, empty QueryExpansionConfig is considered as a default in QueryRewriting
    default Converter<T> createConverter(final QueryConfig queryConfig) {
        return createConverter(queryConfig, QueryExpansionConfig.empty());
    }

    Converter<T> createConverter(final QueryConfig queryConfig, final QueryExpansionConfig<T> queryExpansionConfig);

}
