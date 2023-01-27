package querqy.converter.generic;

import lombok.Builder;
import querqy.FieldConfig;
import querqy.QueryConfig;
import querqy.converter.generic.builder.TermQueryBuilder;
import querqy.converter.generic.model.TermQueryDefinition;
import querqy.model.BoostedTerm;
import querqy.model.Term;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class GenericTermConverter<T> {

    private final TermQueryBuilder<T> termQueryBuilder;

    private final QueryConfig queryConfig;

    public List<T> convert(final Term term) {
        return term.getField() == null
                ? convertUsingQueryConfig(term)
                : convertTermForField(term.getField(), term);
    }

    private List<T> convertUsingQueryConfig(final Term term) {
        return queryConfig.getFields().stream()
                .map(fieldConfig -> createQueryDefinition(fieldConfig, term))
                .map(termQueryBuilder::build)
                .collect(Collectors.toList());
    }

    public List<T> convertTermForField(final String fieldName, final Term term) {
        final TermQueryDefinition termQueryDefinition = createQueryDefinition(
                FieldConfig.fromFieldName(fieldName), term);

        final T convertedTerm = termQueryBuilder.build(termQueryDefinition);
        return List.of(convertedTerm);
    }

    private TermQueryDefinition createQueryDefinition(final FieldConfig fieldConfig, final Term term) {
        return TermQueryDefinition.builder()
                .isConstantScoreQuery(true)
                .term(term.getValue().toString())
                .termBoost(getTermBoost(term))
                .fieldConfig(fieldConfig)
                .build();
    }

    private float getTermBoost(final Term term) {
        if (term instanceof BoostedTerm) {
            return ((BoostedTerm) term).getBoost();

        } else {
            return 1f;
        }
    }
}
