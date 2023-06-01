package querqy.converter.generic;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import querqy.QueryExpansionConfig;
import querqy.WeightedQuery;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;
import querqy.converter.generic.builder.QueryStringQueryBuilder;
import querqy.converter.generic.builder.WrappedQueryBuilder;
import querqy.converter.generic.model.BooleanQueryDefinition;
import querqy.converter.generic.model.ExpandedQueryDefinition;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder(toBuilder = true)
public class GenericExpandedQueryConverter<T> {

    @NonNull private final BooleanQueryBuilder<T> booleanQueryBuilder;
    @NonNull private final ConstantScoreQueryBuilder<T> constantScoreQueryBuilder;
    @NonNull private final WrappedQueryBuilder<T> wrappedQueryBuilder;
    @NonNull private final QueryStringQueryBuilder<T> queryStringQueryBuilder;

    @NonNull private final QueryExpansionConfig<T> queryExpansionConfig;

    public T convert(final ExpandedQueryDefinition<T> expandedQueryDefinition) {
        final _GenericExpandedQueryConverter<T> converter = _GenericExpandedQueryConverter.of(
                booleanQueryBuilder, constantScoreQueryBuilder, queryStringQueryBuilder, queryExpansionConfig, expandedQueryDefinition);
        final T convertedQuery = converter.convert();

        return wrappedQueryBuilder.wrap(convertedQuery);
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class _GenericExpandedQueryConverter<T> {

        @NonNull private final BooleanQueryBuilder<T> booleanQueryBuilder;
        @NonNull private final ConstantScoreQueryBuilder<T> constantScoreQueryBuilder;
        @NonNull private final QueryStringQueryBuilder<T> queryStringQueryBuilder;

        @NonNull private final QueryExpansionConfig<T> queryExpansionConfig;
        @NonNull private final ExpandedQueryDefinition<T> expandedQueryDefinition;

        @NonNull private final BooleanQueryDefinition.BooleanQueryDefinitionBuilder<T> queryDefinitionBuilder = BooleanQueryDefinition.builder();

        public T convert() {
            final T mainQuery = convertMainQuery();
            return embedAlternativeMatchingQueries(mainQuery);
        }

        public T convertMainQuery() {
            if (allBoostsAndFiltersAreEmpty()) {
                return expandedQueryDefinition.getUserQuery();

            } else {
                addClausesToBuilder();
                final BooleanQueryDefinition<T> booleanQueryDefinition = queryDefinitionBuilder.build();
                return booleanQueryBuilder.build(booleanQueryDefinition);
            }
        }

        private boolean allBoostsAndFiltersAreEmpty() {
            return expandedQueryDefinition.getFilterQueries().isEmpty() &&
                    queryExpansionConfig.getFilterQueries().isEmpty() &&
                    queryExpansionConfig.getFilterStringQueries().isEmpty() &&
                    expandedQueryDefinition.getBoostQueries().isEmpty() &&
                    queryExpansionConfig.getBoostUpQueries().isEmpty() &&
                    queryExpansionConfig.getBoostUpStringQueries().isEmpty();
        }

        private void addClausesToBuilder() {
            queryDefinitionBuilder.mustClause(expandedQueryDefinition.getUserQuery());

            addFilterQueries();
            addBoostQueries();
            addWeightedBoostQueries();
        }

        private void addFilterQueries() {
            if (expandedQueryDefinition.getFilterQueries().size() > 0) {
                queryDefinitionBuilder.filterClauses(expandedQueryDefinition.getFilterQueries());
            }

            if (queryExpansionConfig.getFilterQueries().size() > 0) {
                queryDefinitionBuilder.filterClauses(queryExpansionConfig.getFilterQueries());
            }

            if (queryExpansionConfig.getFilterStringQueries().size() > 0) {
                final List<T> convertedQueries = convertStringQueries(queryExpansionConfig.getFilterStringQueries());
                queryDefinitionBuilder.filterClauses(convertedQueries);
            }
        }

        private List<T> convertStringQueries(final List<String> stringQueries) {
            return stringQueries.stream()
                    .map(this::convertStringQuery)
                    .collect(Collectors.toList());
        }

        private T convertStringQuery(final String query) {
            return queryStringQueryBuilder.build(query);
        }

        private void addBoostQueries() {
            if (expandedQueryDefinition.getBoostQueries().size() > 0) {
                queryDefinitionBuilder.shouldClauses(expandedQueryDefinition.getBoostQueries());
            }
        }

        private void addWeightedBoostQueries() {
            if (queryExpansionConfig.getBoostUpQueries().size() > 0) {
                final List<T> convertedQueries = convertWeightedQueries(queryExpansionConfig.getBoostUpQueries());
                queryDefinitionBuilder.shouldClauses(convertedQueries);
            }

            if (queryExpansionConfig.getBoostUpStringQueries().size() > 0) {
                final List<T> convertedQueries = convertWeightedStringQueries(queryExpansionConfig.getBoostUpStringQueries());
                queryDefinitionBuilder.shouldClauses(convertedQueries);
            }
        }

        private List<T> convertWeightedQueries(final List<WeightedQuery<T>> queries) {
            return queries.stream()
                    .map(this::convertWeightedQuery)
                    .collect(Collectors.toList());
        }

        private T convertWeightedQuery(final WeightedQuery<T> query) {
            return query.getWeight().isPresent()
                    ? constantScoreQueryBuilder.build(query.getQuery(), query.getWeight().get())
                    : query.getQuery();
        }

        private List<T> convertWeightedStringQueries(final List<WeightedQuery<String>> queries) {
            return queries.stream()
                    .map(this::convertWeightedStringQuery)
                    .collect(Collectors.toList());
        }

        private T convertWeightedStringQuery(final WeightedQuery<String> query) {
            final T convertedQuery = queryStringQueryBuilder.build(query.getQuery());

            return query.getWeight().isPresent()
                    ? constantScoreQueryBuilder.build(convertedQuery, query.getWeight().get())
                    : convertedQuery;
        }

        private T embedAlternativeMatchingQueries(final T mainQuery) {
            if (queryExpansionConfig.getAlternativeMatchingQueries().size() > 0 ||
                    queryExpansionConfig.getAlternativeMatchingStringQueries().size() > 0) {
                return createAlternativeMatchingBoolQuery(mainQuery);

            } else {
                return mainQuery;
            }
        }

        private T createAlternativeMatchingBoolQuery(final T mainQuery) {
            final BooleanQueryDefinition<T> queryDefinitionBuilder = BooleanQueryDefinition.<T>builder()
                    .shouldClause(mainQuery)
                    .shouldClauses(createAlternativeMatchingQueries())
                    .build();

            return booleanQueryBuilder.build(queryDefinitionBuilder);
        }

        private List<T> createAlternativeMatchingQueries() {
            final List<T> convertedQueries = convertWeightedQueries(queryExpansionConfig.getAlternativeMatchingQueries());
            final List<T> convertedStringQueries = convertWeightedStringQueries(queryExpansionConfig.getAlternativeMatchingStringQueries());

            return Stream.concat(convertedQueries.stream(), convertedStringQueries.stream()).collect(Collectors.toList());
        }
    }

}
