package support;

import java.util.List;
import java.util.stream.Collectors;

public class BuilderSupport {

    public static String wrap(final String functionName, final List<String> clauses) {
        final List<String> filteredClauses = clauses.stream().filter(str -> !str.isEmpty()).collect(Collectors.toList());

        if (clauses.size() > 0) {
            return functionName + "(" + String.join(",", filteredClauses) + ")";

        } else {
            return "";
        }
    }
}
