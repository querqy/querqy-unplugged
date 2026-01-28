package querqy.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.Set;

@Builder
@Getter
public class RewrittenQuery<T> {

    private final T convertedQuery;

    @NonNull
    private final RewrittenQuerqyQuery rewrittenQuerqyQuery;
    
    public Set<Object> getDecorations() {
        return rewrittenQuerqyQuery.getDecorations();
    }

    public Map<String, Object> getNamedDecorations() {
        return rewrittenQuerqyQuery.getNamedDecorations();
    }

}
