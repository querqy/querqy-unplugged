package querqy.converter.solr.map.boost;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConvertedBoostQuery {
    private final Object boostFunctionQuery;
    private final String queryReference;
    private final Object query;
}
