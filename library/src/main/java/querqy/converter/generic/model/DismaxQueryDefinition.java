package querqy.converter.generic.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
@ToString
public class DismaxQueryDefinition<T> {

    private final List<T> dismaxClauses;
    private final Float tie;

    public Optional<Float> getTie() {
        return Optional.ofNullable(tie);
    }


}
