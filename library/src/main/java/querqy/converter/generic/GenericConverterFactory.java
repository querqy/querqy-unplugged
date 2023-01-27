package querqy.converter.generic;

import lombok.Builder;
import lombok.NonNull;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.DismaxQueryBuilder;
import querqy.converter.generic.builder.ExpandedQueryBuilder;
import querqy.converter.generic.builder.MatchAllQueryBuilder;
import querqy.converter.generic.builder.RawQueryBuilder;
import querqy.converter.generic.builder.TermQueryBuilder;

@Builder
public class GenericConverterFactory<T> implements ConverterFactory<T> {

    @NonNull private final ExpandedQueryBuilder<T> expandedQueryBuilder;
    @NonNull private final BooleanQueryBuilder<T> booleanQueryBuilder;
    @NonNull private final DismaxQueryBuilder<T> dismaxQueryBuilder;
    @NonNull private final TermQueryBuilder<T> termQueryBuilder;
    @NonNull private final MatchAllQueryBuilder<T> matchAllQueryBuilder;
    @NonNull private final RawQueryBuilder<T> rawQueryBuilder;

    @Override
    public Converter<T> createConverter(final QueryConfig queryConfig) {
        return createGenericConverter(queryConfig);
    }

    private GenericConverter<T> createGenericConverter(final QueryConfig queryConfig) {
        return GenericConverter.<T>builder()
                .expandedQueryBuilder(expandedQueryBuilder)
                .genericQuerqyQueryConverter(createGenericQuerqyQueryConverter(queryConfig))
                .build();
    }

    private GenericQuerqyQueryConverter<T> createGenericQuerqyQueryConverter(final QueryConfig queryConfig) {
        return GenericQuerqyQueryConverter.<T>builder()
                .booleanQueryBuilder(booleanQueryBuilder)
                .dismaxQueryBuilder(dismaxQueryBuilder)
                .genericTermConverter(createGenericTermConverter(queryConfig))
                .matchAllQueryBuilder(matchAllQueryBuilder)
                .rawQueryBuilder(rawQueryBuilder)
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
