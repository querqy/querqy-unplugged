package querqy.domain;

import lombok.Builder;
import lombok.Getter;
import querqy.model.ExpandedQuery;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Builder
@Getter
public class RewrittenQuerqyQuery {
    private final ExpandedQuery query;
    private final Map<String, Object> rewriteLogging;
    @Builder.Default
    private Set<Object> decorations = Collections.emptySet();
    @Builder.Default
    private Map<String, Object> namedDecorations = Collections.emptyMap();
}
