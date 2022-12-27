package querqy;

import java.util.Map;

public interface QueryTypeConfig {

    String getTypeName();
    String getQueryParamName();
    String getFieldParamName();
    Map<String, Object> getConstantParams();

}
