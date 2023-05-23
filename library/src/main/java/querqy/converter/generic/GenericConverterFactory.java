package querqy.converter.generic;

import lombok.Builder;
import lombok.NonNull;
import querqy.BoostConfig;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;
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
    @NonNull private final ConstantScoreQueryBuilder<T> constantScoreQueryBuilder;
    @NonNull private final TermQueryBuilder<T> termQueryBuilder;
    @NonNull private final MatchAllQueryBuilder<T> matchAllQueryBuilder;
    @NonNull private final RawQueryBuilder<T> rawQueryBuilder;
    @NonNull private final BoostQueryBuilder<T> boostQueryBuilder;

    @Override
    public Converter<T> createConverter(final QueryConfig queryConfig) {
        return createGenericConverter(queryConfig);
    }

    private GenericConverter<T> createGenericConverter(final QueryConfig queryConfig) {
        final GenericQuerqyQueryConverter<T> genericQuerqyQueryConverter = createGenericQuerqyQueryConverter(queryConfig);
        final GenericBoostConverter<T> genericBoostConverter = createGenericBoostConverter(
                genericQuerqyQueryConverter, queryConfig.getBoostConfig());

        return GenericConverter.<T>builder()
                .expandedQueryBuilder(expandedQueryBuilder)
                .genericQuerqyQueryConverter(genericQuerqyQueryConverter)
                .genericBoostConverter(genericBoostConverter)
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
                .constantScoreQueryBuilder(constantScoreQueryBuilder)
                .termQueryBuilder(termQueryBuilder)
                .queryConfig(queryConfig)
                .build();
    }

    private GenericBoostConverter<T> createGenericBoostConverter(
            final GenericQuerqyQueryConverter<T> genericQuerqyQueryConverter,
            final BoostConfig boostConfig
            ) {
        return GenericBoostConverter.<T>builder()
                .boostQueryBuilder(boostQueryBuilder)
                .genericQuerqyQueryConverter(genericQuerqyQueryConverter)
                .booleanQueryBuilder(booleanQueryBuilder)
                .constantScoreQueryBuilder(constantScoreQueryBuilder)
                .boostConfig(boostConfig)
                .build();
    }
}
