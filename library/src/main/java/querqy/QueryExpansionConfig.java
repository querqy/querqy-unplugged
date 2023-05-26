package querqy;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Builder
@Getter
public class QueryExpansionConfig<T> {

    private static final QueryExpansionConfig<?> EMPTY = QueryExpansionConfig.builder().build();

    @Singular private final List<T> filterQueries;
    @Singular private final List<T> boostUpQueries;
    @Singular private final List<T> alternativeMatchingQueries;

    public static <T> QueryExpansionConfig<T> empty() {
        //noinspection unchecked
        return (QueryExpansionConfig<T>) EMPTY;
    }

}
