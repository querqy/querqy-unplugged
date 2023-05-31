package querqy;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@Builder
public class WeightedQuery<T> {

    @Getter
    @NonNull
    private final T query;

    private final Float weight;

    public Optional<Float> getWeight() {
        return Optional.ofNullable(weight);
    }
}
