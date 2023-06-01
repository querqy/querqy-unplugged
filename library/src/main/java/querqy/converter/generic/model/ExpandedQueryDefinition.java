package querqy.converter.generic.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Builder
@Getter
public class ExpandedQueryDefinition<T> {

    private final T userQuery;
    @Singular private final List<T> filterQueries;
    @Singular private final List<T> boostQueries;

}
