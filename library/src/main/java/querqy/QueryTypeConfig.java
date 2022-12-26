package querqy;

import java.util.Map;

public interface QueryTypeConfig {

    String getTypeName();
    String getQueryParameterName();
    Map<String, Object> getConstantParameters();

}
