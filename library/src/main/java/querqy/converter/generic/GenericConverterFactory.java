package querqy.converter.generic;

import lombok.Builder;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.DismaxQueryBuilder;
import querqy.converter.generic.builder.TermQueryBuilder;

@Builder
public class GenericConverterFactory<T> implements ConverterFactory<T> {

    private final BooleanQueryBuilder<T> booleanQueryBuilder;
    private final DismaxQueryBuilder<T> dismaxQueryBuilder;
    private final TermQueryBuilder<T> termQueryBuilder;

    @Override
    public Converter<T> createConverter(final QueryConfig queryConfig) {
        return createGenericConverter(queryConfig);
    }

    private GenericConverter<T> createGenericConverter(final QueryConfig queryConfig) {
        return GenericConverter.<T>builder()
                .genericQuerqyQueryConverter(createGenericQuerqyQueryConverter(queryConfig))
                .build();
    }

    private GenericQuerqyQueryConverter<T> createGenericQuerqyQueryConverter(final QueryConfig queryConfig) {
        return GenericQuerqyQueryConverter.<T>builder()
                .booleanQueryBuilder(booleanQueryBuilder)
                .dismaxQueryBuilder(dismaxQueryBuilder)
                .genericTermConverter(createGenericTermConverter(queryConfig))
                .queryConfig(queryConfig)
                .build();

    }

    private GenericTermConverter<T> createGenericTermConverter(final QueryConfig queryConfig) {
        return GenericTermConverter.<T>builder()
                .termQueryBuilder(termQueryBuilder)
                .queryConfig(queryConfig)
                .build();
    }
}
