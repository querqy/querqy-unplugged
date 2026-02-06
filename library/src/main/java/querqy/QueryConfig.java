package querqy;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Builder(toBuilder = true)
@Getter
public class QueryConfig {

    private final List<FieldConfig> fields;

    private final Float tie;
    private final String minimumShouldMatch;
    private Map<String, String[]> params;

    @Builder.Default private final QueryNodesConfig queryNodesConfig = QueryNodesConfig.empty();
    @Builder.Default private final BoostConfig boostConfig = BoostConfig.defaultConfig();
    @Builder.Default private final boolean isConstantScoresQuery = true;

    public Optional<String> getMinimumShouldMatch() {
        return Optional.ofNullable(minimumShouldMatch);
    }

    public Optional<Float> getTie() {
        return Optional.ofNullable(tie);
    }

    @Deprecated // Be careful - might lead to NPE
    public static QueryConfig empty() {
        return QueryConfig.builder().build();
    }

    public static class QueryConfigBuilder {
        private final Set<String> fieldNames = new HashSet<>();

        public QueryConfig.QueryConfigBuilder field(final String fieldName, final float weight) {
            return field(
                    FieldConfig.builder()
                            .fieldName(fieldName)
                            .weight(weight)
                            .build()
            );
        }

        public QueryConfig.QueryConfigBuilder field(final FieldConfig field) {
            if (this.fields == null) {
                this.fields = new ArrayList<>();
            }

            final String fieldName = field.getFieldName();
            if (fieldNames.contains(fieldName)) {
                throw new IllegalArgumentException("Duplicate field " + fieldName);
            }
            fieldNames.add(fieldName);

            this.fields.add(field);
            return this;
        }

        public QueryConfig.QueryConfigBuilder addRewriterParam(@NonNull final String rewriterId,
                                                               @NonNull final String name, final String value) {

            final String key = "querqy." + rewriterId + "." + name;

            if (params == null) {
                params = new HashMap<>();
                params.put(key, new String[] {value});
            } else {
                String[] values = params.get(key);
                if (values == null) {
                    params.put(key, new String[] {value});
                } else {
                    String[] extended = Arrays.copyOf(values, values.length + 1);
                    extended[values.length - 1] = value;
                    params.put(key, extended);
                }

            }

            return this;
        }

        public QueryConfig.QueryConfigBuilder addRewriterParam(@NonNull final String rewriterId,
                                                               @NonNull final String name, final int value) {
            return addRewriterParam(rewriterId, name, Integer.toString(value));
        }

        public QueryConfig.QueryConfigBuilder addRewriterParam(@NonNull final String rewriterId,
                                                               @NonNull final String name, final boolean value) {
            return addRewriterParam(rewriterId, name, Boolean.toString(value));
        }

        public QueryConfig.QueryConfigBuilder setRewriterParams(@NonNull final String rewriterId,
                                                               @NonNull final String name, final String[] values) {

            final String key = "querqy." + rewriterId + "." + name;

            if (params == null) {
                params = new HashMap<>();
            }
            params.put(key, values);

            return this;
        }
    }

}
