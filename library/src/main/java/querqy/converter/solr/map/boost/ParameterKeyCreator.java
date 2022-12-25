package querqy.converter.solr.map.boost;

import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class ParameterKeyCreator {
    private static final String PARAM_PREFIX = "_querqy_boost_";

    private int parameterCount = 0;

    public String createKey() {
        return PARAM_PREFIX + parameterCount++;
    }

}
