package querqy.converter.generic;

import org.junit.Before;
import org.junit.Test;
import querqy.QueryExpansionConfig;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;
import querqy.converter.generic.builder.QueryStringQueryBuilder;
import querqy.converter.generic.builder.WrappedQueryBuilder;
import querqy.converter.generic.model.ExpandedQueryDefinition;
import support.StringBooleanQueryBuilder;
import support.StringConstantScoreQueryBuilder;
import support.StringQueryStringQueryBuilder;
import support.StringWrappedQueryBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericExpandedQueryConverterTest {

    private final WrappedQueryBuilder<String> identityWrappedQueryBuilder = x -> x;
    private final QueryStringQueryBuilder<String> identityQueryStringQueryBuilder = x -> x;

    private final QueryExpansionConfig<String> emptyQueryExpansionConfig = QueryExpansionConfig.empty();

    private final ExpandedQueryDefinition.ExpandedQueryDefinitionBuilder<String> queryDefinitionBuilder = ExpandedQueryDefinition.<String>builder()
            .userQuery("user");

    private final GenericExpandedQueryConverter.GenericExpandedQueryConverterBuilder<String> converterBuilder = GenericExpandedQueryConverter.builder();

    @Before
    public void setup() {
        final BooleanQueryBuilder<String> booleanQueryBuilder = StringBooleanQueryBuilder.create();
        final ConstantScoreQueryBuilder<String> constantScoreQueryBuilder = StringConstantScoreQueryBuilder.create();

        converterBuilder.booleanQueryBuilder(booleanQueryBuilder)
                .constantScoreQueryBuilder(constantScoreQueryBuilder)
                .wrappedQueryBuilder(identityWrappedQueryBuilder)
                .queryStringQueryBuilder(identityQueryStringQueryBuilder)
                .queryExpansionConfig(emptyQueryExpansionConfig);
    }

    @Test
    public void testThat_userQueryIsDirectlyReturned_forOnlyUserQueryGiven() {
        final String convertedQuery = converterBuilder.build().convert(queryDefinitionBuilder.build());
        assertThat(convertedQuery).isEqualTo("user");
    }

    @Test
    public void testThat_userQueryIsWrapped_forGivenWrappedQueryBuilder() {
        final String convertedQuery = converterBuilder
                .wrappedQueryBuilder(StringWrappedQueryBuilder.create())
                .build()
                .convert(queryDefinitionBuilder.build());

        assertThat(convertedQuery).isEqualTo("wrapped(user)");
    }

    @Test
    public void testThat_userQueryIsEmbedded_forGivenAlternativeMatchingQueries() {
        final String convertedQuery = converterBuilder
                .queryExpansionConfig(
                        QueryExpansionConfig.<String>builder()
                                .addAlternativeMatchingQuery("alt")
                                .addAlternativeMatchingQuery("alt2")
                                .build())
                .build()
                .convert(queryDefinitionBuilder.build());

        assertThat(convertedQuery).isEqualTo("bool(should(user,alt,alt2))");
    }

    @Test
    public void testThat_userQueryIsEmbedded_forGivenAlternativeMatchingStringQuery() {
        final String convertedQuery = converterBuilder
                .queryStringQueryBuilder(StringQueryStringQueryBuilder.create())
                .queryExpansionConfig(
                        QueryExpansionConfig.<String>builder()
                                .addAlternativeMatchingStringQuery("alt")
                                .build())
                .build()
                .convert(queryDefinitionBuilder.build());

        assertThat(convertedQuery).isEqualTo("bool(should(user,converted(alt)))");
    }

    @Test
    public void testThat_userQueryIsEnhanced_forGivenBoostQuery() {
        final String convertedQuery = converterBuilder
                .queryExpansionConfig(
                        QueryExpansionConfig.<String>builder()
                                .addBoostUpQuery("boost")
                                .build())
                .build()
                .convert(queryDefinitionBuilder.build());

        assertThat(convertedQuery).isEqualTo("bool(must(user),should(boost))");
    }

    @Test
    public void testThat_userQueryIsEnhanced_forGivenStringBoostQuery() {
        final String convertedQuery = converterBuilder
                .queryStringQueryBuilder(StringQueryStringQueryBuilder.create())
                .queryExpansionConfig(
                        QueryExpansionConfig.<String>builder()
                                .addBoostUpStringQuery("boost")
                                .build())
                .build()
                .convert(queryDefinitionBuilder.build());

        assertThat(convertedQuery).isEqualTo("bool(must(user),should(converted(boost)))");
    }

    @Test
    public void testThat_userQueryIsEnhanced_forGivenFilterQuery() {
        final String convertedQuery = converterBuilder
                .queryExpansionConfig(
                        QueryExpansionConfig.<String>builder()
                                .filterQuery("filter")
                                .build())
                .build()
                .convert(queryDefinitionBuilder.build());

        assertThat(convertedQuery).isEqualTo("bool(must(user),filter(filter))");
    }

    @Test
    public void testThat_userQueryIsEnhanced_forGivenStringFilterQuery() {
        final String convertedQuery = converterBuilder
                .queryStringQueryBuilder(StringQueryStringQueryBuilder.create())
                .queryExpansionConfig(
                        QueryExpansionConfig.<String>builder()
                                .filterStringQuery("filter")
                                .build())
                .build()
                .convert(queryDefinitionBuilder.build());

        assertThat(convertedQuery).isEqualTo("bool(must(user),filter(converted(filter)))");
    }
}
