package querqy.converter.solr.map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BoostMapEntry {

    private final Object boostFunctionQuery;
    private final String convertedQueryReference;
    private final Object convertedQuery;

}
