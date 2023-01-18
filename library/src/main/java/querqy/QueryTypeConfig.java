package querqy;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Builder
@Getter
public class QueryTypeConfig {

    private final String typeName;
    private final String queryParamName;
    private final String fieldParamName;
    @Singular private final Map<String, Object> constantParams;

}
