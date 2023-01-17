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

    public enum Occur {
        MUST, SHOULD, MUST_NOT
    }

    @Singular private final List<T> dismaxQueries;
    private final Occur occur;
    private final float boost;

    private final String minimumShouldMatch;

    public Optional<String> getMinimumShouldMatch() {
        return Optional.ofNullable(minimumShouldMatch);
    }


}
