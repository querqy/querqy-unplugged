package querqy.domain;

import lombok.Builder;
import lombok.Getter;
import querqy.model.ExpandedQuery;

import java.util.Map;

@Builder
@Getter
public class RewrittenQuerqyQuery {
    private final ExpandedQuery query;
    private final Map<String, Object> rewritingTracking;
}
