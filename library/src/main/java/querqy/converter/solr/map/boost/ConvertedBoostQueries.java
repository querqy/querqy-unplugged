package querqy.converter.solr.map.boost;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class ConvertedBoostQueries {

    private final List<Object> boostFunctionQueries;
    private final Map<String, Object> referencedQueries;

    public boolean isEmpty() {
        return boostFunctionQueries.isEmpty();
    }

    public static ConvertedBoostQueries empty() {
        return ConvertedBoostQueries.builder()
                .boostFunctionQueries(List.of())
                .referencedQueries(Map.of())
                .build();
    }
}
