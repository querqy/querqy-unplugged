package querqy;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Builder
@Getter
public class QueryExpansionConfig<T> {

    private static final QueryExpansionConfig<?> EMPTY = QueryExpansionConfig.builder().build();

    @Singular private final List<T> filterQueries;
    @Singular private final List<WeightedQuery<T>> boostUpQueries;
    @Singular private final List<WeightedQuery<T>> alternativeMatchingQueries;

    @Singular private final List<String> filterStringQueries;
    @Singular private final List<WeightedQuery<String>> boostUpStringQueries;
    @Singular private final List<WeightedQuery<String>> alternativeMatchingStringQueries;

    public static <T> QueryExpansionConfig<T> empty() {
        //noinspection unchecked
        return (QueryExpansionConfig<T>) EMPTY;
    }

    public static class QueryExpansionConfigBuilder<T> {
        public QueryExpansionConfig.QueryExpansionConfigBuilder<T> addBoostUpQuery(final T query) {
            return this.addBoostUpQuery(query, null);
        }

        public QueryExpansionConfig.QueryExpansionConfigBuilder<T> addBoostUpQuery(final T query, final Float weight) {
            this.boostUpQuery(
                    WeightedQuery.<T>builder()
                            .query(query)
                            .weight(weight)
                            .build()
            );
            return this;
        }

        public QueryExpansionConfig.QueryExpansionConfigBuilder<T> addBoostUpStringQuery(final String query) {
            return this.addBoostUpStringQuery(query, null);
        }

        public QueryExpansionConfig.QueryExpansionConfigBuilder<T> addBoostUpStringQuery(final String query, final Float weight) {
            this.boostUpStringQuery(
                    WeightedQuery.<String>builder()
                            .query(query)
                            .weight(weight)
                            .build()
            );
            return this;
        }

        public QueryExpansionConfig.QueryExpansionConfigBuilder<T> addAlternativeMatchingQuery(final T query) {
            return this.addAlternativeMatchingQuery(query, null);
        }

        public QueryExpansionConfig.QueryExpansionConfigBuilder<T> addAlternativeMatchingQuery(final T query, final Float weight) {
            this.alternativeMatchingQuery(
                    WeightedQuery.<T>builder()
                            .query(query)
                            .weight(weight)
                            .build()
            );
            return this;
        }

        public QueryExpansionConfig.QueryExpansionConfigBuilder<T> addAlternativeMatchingStringQuery(final String query) {
            return this.addAlternativeMatchingStringQuery(query, null);
        }

        public QueryExpansionConfig.QueryExpansionConfigBuilder<T> addAlternativeMatchingStringQuery(final String query, final Float weight) {
            this.alternativeMatchingStringQuery(
                    WeightedQuery.<String>builder()
                            .query(query)
                            .weight(weight)
                            .build()
            );
            return this;
        }
    }

}
