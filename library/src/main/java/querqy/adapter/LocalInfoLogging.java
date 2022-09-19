package querqy.adapter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import querqy.infologging.InfoLogging;
import querqy.rewrite.SearchEngineRequestAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(staticName = "create")
public class LocalInfoLogging implements InfoLogging {

    private static final String APPLIED_RULES_KEY = "APPLIED_RULES";

    @Getter
    private final Map<String, List<Object>> infoLogging = new LinkedHashMap<>();

    @Override
    public void log(final Object message, final String rewriterId,
                    final SearchEngineRequestAdapter searchEngineRequestAdapter) {

        if (message instanceof IdentityHashMap) {
            final Object messageValue = ((IdentityHashMap<?, ?>) message).get(APPLIED_RULES_KEY);
            final List<Object> rewriterValues = infoLogging.computeIfAbsent(rewriterId, k -> new ArrayList<>());

            if (messageValue instanceof Collection) {
                rewriterValues.addAll((Collection<?>) messageValue);

            } else {
                rewriterValues.add(messageValue);
            }
        }
    }

    @Override
    public void endOfRequest(final SearchEngineRequestAdapter searchEngineRequestAdapter) {}

    @Override
    public boolean isLoggingEnabledForRewriter(final String rewriterId) {
        return true;
    }
}
