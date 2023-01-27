package querqy.converter.generic.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ExpandedQueryDefinition<T> {

    private final T userQuery;
    private final List<T> filterQueries;

}
