package querqy.adapter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import querqy.model.ExpandedQuery;

import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
@Getter
public class RewrittenQuery {
    private final ExpandedQuery query;
    private final Map<String, Object> context;
}
