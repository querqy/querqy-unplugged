package querqy.converter.generic;

import lombok.Builder;
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
        return queryConfig.getFields().stream()
                .map(fieldConfig -> TermQueryDefinition.builder()
                        .isConstantScoreQuery(true)
                        .term(term.getValue().toString())
                        .termBoost(getTermBoost(term))
                        .fieldConfig(fieldConfig)
                        .build()
                )
                .map(termQueryBuilder::build)
                .collect(Collectors.toList());
    }

    private float getTermBoost(final Term term) {
        if (term instanceof BoostedTerm) {
            return ((BoostedTerm) term).getBoost();

        } else {
            return 1f;
        }
    }
}
