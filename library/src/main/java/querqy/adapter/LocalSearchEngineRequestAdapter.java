package querqy.adapter;

import lombok.Builder;
import lombok.Setter;
import lombok.Singular;
import querqy.infologging.InfoLoggingContext;
import querqy.rewrite.RewriteChain;
import querqy.rewrite.SearchEngineRequestAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Builder
public class LocalSearchEngineRequestAdapter implements SearchEngineRequestAdapter {

    private final RewriteChain rewriteChain;
    private final Map<String, String[]> params;

    @Setter
    private InfoLoggingContext infoLoggingContext;

    private final Map<String, Object> context = new HashMap<>();

    private static final String NUMBER_FORMAT_EXCEPTION_TEMPLATE = "Parameter %s must be of type %s";

    @Override
    public RewriteChain getRewriteChain() {
        return rewriteChain;
    }

    @Override
    public Map<String, Object> getContext() {
        return context;
    }

    @Override
    public Optional<String> getRequestParam(final String name) {
        final String[] paramValues = params.get(name);
        return paramValues != null && paramValues.length > 0 ? Optional.ofNullable(params.get(name)[0]) : Optional.empty();
    }

    @Override
    public String[] getRequestParams(final String name) {
        final String[] paramValues = params.get(name);
        return paramValues != null ? paramValues : new String[0];
    }

    @Override
    public Optional<Boolean> getBooleanRequestParam(final String name) {
        final String[] param = params.get(name);
        return param != null && param.length > 0 ? Optional.of(Boolean.parseBoolean(param[0])) : Optional.empty();
    }

    @Override
    public Optional<Integer> getIntegerRequestParam(final String name) {
        final String[] param = params.get(name);

        if (param != null && param.length > 0) {
            try {
                return Optional.of(Integer.parseInt(param[0]));

            } catch (NumberFormatException nfe) {
                throw new NumberFormatException(
                        String.format(NUMBER_FORMAT_EXCEPTION_TEMPLATE, name, Integer.class.getName()));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Float> getFloatRequestParam(final String name) {
        final String[] param = params.get(name);

        if (param != null && param.length > 0) {
            try {
                return Optional.of(Float.parseFloat(param[0]));

            } catch (NumberFormatException nfe) {
                throw new NumberFormatException(
                        String.format(NUMBER_FORMAT_EXCEPTION_TEMPLATE, name, Float.class.getName()));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Double> getDoubleRequestParam(final String name) {
        final String[] param = params.get(name);

        if (param != null && param.length > 0) {
            try {
                return Optional.of(Double.parseDouble(param[0]));

            } catch (NumberFormatException nfe) {
                throw new NumberFormatException(
                        String.format(NUMBER_FORMAT_EXCEPTION_TEMPLATE, name, Double.class.getName()));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<InfoLoggingContext> getInfoLoggingContext() {
        return Optional.ofNullable(infoLoggingContext);
    }

    @Override
    public boolean isDebugQuery() {
        return false;
    }
}
