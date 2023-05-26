package querqy.converter.generic.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
@ToString
public class BooleanQueryDefinition<T> {

    @Singular private final List<T> shouldClauses;
    @Singular private final List<T> mustClauses;
    @Singular private final List<T> filterClauses;
    @Singular private final List<T> mustNotClauses;

    private final Float boost;
    private final String minimumShouldMatch;

    public Optional<String> getMinimumShouldMatch() {
        return Optional.ofNullable(minimumShouldMatch);
    }
    public Optional<Float> getBoost() {
        return Optional.ofNullable(boost);
    }

    public static class BooleanQueryDefinitionBuilder<T> {
        public int numberOfMustClauses() {
            return this.mustClauses == null ? 0 : this.mustClauses.size();
        }

        public int numberOfShouldClauses() {
            return this.shouldClauses == null ? 0 : this.shouldClauses.size();
        }
    }


}
