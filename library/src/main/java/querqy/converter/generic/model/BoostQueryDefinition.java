package querqy.converter.generic.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import querqy.BoostConfig;

@Builder
@Getter
public class BoostQueryDefinition<T> {

    @NonNull private final T query;
    @NonNull private final BoostConfig boostConfig;
    @NonNull private final Float boost;


}
