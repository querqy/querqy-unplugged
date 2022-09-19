package querqy.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RewrittenQuery<T> {

    private final T convertedQuery;
    private final RewrittenQuerqyQuery rewrittenQuerqyQuery;

}
