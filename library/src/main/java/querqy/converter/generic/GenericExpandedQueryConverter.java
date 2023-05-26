package querqy.converter.generic;

import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.WrappedQueryBuilder;
import querqy.converter.generic.model.BooleanQueryDefinition;
import querqy.converter.generic.model.ExpandedQueryDefinition;

@RequiredArgsConstructor(staticName = "of")
public class GenericExpandedQueryConverter<T> {

    private final BooleanQueryBuilder<T> booleanQueryBuilder;
    private final WrappedQueryBuilder<T> wrappedQueryBuilder;

    public T convert(final ExpandedQueryDefinition<T> expandedQueryDefinition) {
        final _GenericExpandedQueryConverter<T> converter = _GenericExpandedQueryConverter.of(booleanQueryBuilder, expandedQueryDefinition);
        final T convertedQuery = converter.convert();

        return wrappedQueryBuilder.wrap(convertedQuery);
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class _GenericExpandedQueryConverter<T> {

        private final BooleanQueryBuilder<T> booleanQueryBuilder;
        private final ExpandedQueryDefinition<T> definition;

        private final BooleanQueryDefinition.BooleanQueryDefinitionBuilder<T> builder = BooleanQueryDefinition.builder();

        public T convert() {
            if (definition.getFilterQueries().isEmpty() && definition.getBoostQueries().isEmpty()) {
                return definition.getUserQuery();

            } else {
                addClausesToBuilder();
                final BooleanQueryDefinition<T> booleanQueryDefinition = builder.build();
                return booleanQueryBuilder.build(booleanQueryDefinition);
            }
        }

        private void addClausesToBuilder() {
            builder.mustClause(definition.getUserQuery());

            addFilterQueries();
            addBoostQueries();
        }

        private void addFilterQueries() {
            if (definition.getFilterQueries().size() > 0) {
                builder.filterClauses(definition.getFilterQueries());
            }
        }

        private void addBoostQueries() {
            if (definition.getBoostQueries().size() > 0) {
                builder.shouldClauses(definition.getBoostQueries());
            }
        }

//        private Map<String, Object> buildQuery(final Map<String, Object> query) {
//            final Map<String, Object> wrappedQuery = new HashMap<>();
//            wrappedQuery.put("query", query);
//            wrappedQuery.put("queries", queryReferenceBuilder.getReferences());
//            return wrappedQuery;
//        }
    }

}
