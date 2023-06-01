package querqy.converter.generic;

import lombok.Builder;
import lombok.NonNull;
import querqy.FieldConfig;
import querqy.QueryConfig;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;
import querqy.converter.generic.builder.TermQueryBuilder;
import querqy.converter.generic.model.TermQueryDefinition;
import querqy.model.BoostedTerm;
import querqy.model.Term;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class GenericTermConverter<T> {

    private final ConstantScoreQueryBuilder<T> constantScoreQueryBuilder;
    private final TermQueryBuilder<T> termQueryBuilder;
    private final QueryConfig queryConfig;

    public List<T> convert(final Term term) {
        final _GenericTermConverter<T> converter = _GenericTermConverter.<T>builder()
                .constantScoreQueryBuilder(constantScoreQueryBuilder)
                .termQueryBuilder(termQueryBuilder)
                .queryConfig(queryConfig)
                .term(term)
                .build();
        return converter.convert();
    }

    @Builder
    private static class _GenericTermConverter<T> {
        @NonNull private final ConstantScoreQueryBuilder<T> constantScoreQueryBuilder;
        @NonNull private final TermQueryBuilder<T> termQueryBuilder;
        @NonNull private final QueryConfig queryConfig;
        @NonNull private final Term term;

        public List<T> convert() {
            return term.getField() == null
                    ? convertUsingQueryConfig()
                    : convertTermForField(term.getField());
        }

        private List<T> convertUsingQueryConfig() {
            return queryConfig.getFields().stream()
                    .map(this::buildTermQuery)
                    .collect(Collectors.toList());
        }

        private T buildTermQuery(final FieldConfig fieldConfig) {
            final TermQueryDefinition termQueryDefinition = createQueryDefinition(fieldConfig);
            final T termQuery = termQueryBuilder.build(termQueryDefinition);

            if (queryConfig.isConstantScoresQuery()) {
                return constantScoreQueryBuilder.build(termQuery, getTermBoost() * fieldConfig.getWeight());


            } else {
                throw new UnsupportedOperationException(
                        this.getClass().getName() + " currently only supports creating constant scores queries");
            }
        }

        private TermQueryDefinition createQueryDefinition(final FieldConfig fieldConfig) {
            return TermQueryDefinition.builder()
                    .term(term.getValue().toString())
                    .fieldName(fieldConfig.getFieldName())
                    .fieldConfig(fieldConfig)
                    .build();
        }

        public List<T> convertTermForField(final String fieldName) {
            final TermQueryDefinition termQueryDefinition = createQueryDefinition(
                    FieldConfig.fromFieldName(fieldName));

            final T convertedTerm = termQueryBuilder.build(termQueryDefinition);
            return List.of(convertedTerm);
        }

        private float getTermBoost() {
            if (term instanceof BoostedTerm) {
                return ((BoostedTerm) term).getBoost();

            } else {
                return 1f;
            }
        }
    }
}
