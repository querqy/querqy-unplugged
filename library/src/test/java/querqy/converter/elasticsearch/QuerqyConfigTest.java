package querqy.converter.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import org.junit.Test;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterConfig;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterFactory;
import querqy.converter.elasticsearch.javaclient.NumberUnitQueryCreatorElasticsearch;
import querqy.domain.RewrittenQuery;
import querqy.rewriter.builder.CommonRulesDefinition;
import querqy.rewriter.builder.NumberUnitRulesDefinition;

public class QuerqyConfigTest {

    private static final int DEFAULT_SCALE_FOR_LINEAR_FUNCTIONS = 5;

    static final String NUMBER_UNIT_CONFIG = "{\n" +
            "   \"numberUnitDefinitions\": [\n" +
            "       {\n" +
            "          \"units\": [ { \"term\": \"inch\" } ],\n" +
            "          \"fields\": [ { \"fieldName\": \"screen_size\" , \"scale\":1 } ],\n" +
            "          \"boost\": {\n" +
            "             \"percentageLowerBoundary\": 10,\n" +
            "             \"percentageUpperBoundary\": 10,\n" +
            " \n" +
            "            \"minScoreAtLowerBoundary\": 20,\n" +
            "            \"minScoreAtUpperBoundary\": 20,\n" +
            "\n" +
            "            \"percentageLowerBoundaryExactMatch\": 5,\n" +
            "            \"percentageUpperBoundaryExactMatch\": 5,\n" +
            "\n" +
            "            \"maxScoreForExactMatch\": 40,\n" +
            "            \"additionalScoreForExactMatch\": 15\n" +
            "         },\n" +
            "         \"filter\": {\n" +
            "            \"percentageLowerBoundary\": 20,\n" +
            "            \"percentageUpperBoundary\": 10\n" +
            "         }\n" +
            "      }\n" +
            "   ]\n" +
            "}";





    @Test
    public void testRewriting()  {

        final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.of(
                ESJavaClientConverterConfig.builder()
                        .rawQueryInputType(ESJavaClientConverterConfig.RawQueryInputType.JSON)
                        .build()
        );

        final QuerqyConfig querqyConfig = QuerqyConfig.builder()

                .commonRules(
                        CommonRulesDefinition.builder()
                                .rewriterId("id1")
                                .rules("iphone => \n SYNONYM: apple smartphone\n\niphone =>\nDOWN(5): * {\"term\":{\"category\": \"accessories\"}}")
                                .build()
                )
                .numberUnitRules(NumberUnitRulesDefinition.builder()
                        .rewriterId("id2")
                        .rules(NUMBER_UNIT_CONFIG)
                        .numberUnitQueryCreator(new NumberUnitQueryCreatorElasticsearch(DEFAULT_SCALE_FOR_LINEAR_FUNCTIONS))
                        .build())
                .build();

        final QueryConfig queryConfig = QueryConfig.builder()
                .field("name", 40.0f)
                .field("type", 20.0f)
                .minimumShouldMatch("100%")
                .tie(0.0f)
                .build();

        final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                .querqyConfig(querqyConfig)
                .queryConfig(queryConfig)
                .converterFactory(converterFactory)
                .build();


        final RewrittenQuery<Query> query = queryRewriting.rewriteQuery("iphone 4 inch ");
        System.out.println(query);
        final Query convertedQuery = query.getConvertedQuery();
        System.out.println(convertedQuery);
    }


    private final String CONFIG_ROUNDING = "{\n" +
            "    \"numberUnitDefinitions\": [\n" +
            "        {\n" +
            "            \"units\": [\n" +
            "                {\n" +
            "                    \"term\": \"inches\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"term\": \"inch\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"term\": \"IN\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"fields\": [\n" +
            "                {\n" +
            "                    \"fieldName\": \"dim_in\", \"scale\":1 \n" +
            "                }\n" +
            "            ],\n" +
            "            \"boost\": {\n" +
            "                \"percentageLowerBoundary\": 10,\n" +
            "                \"percentageUpperBoundary\": 10,\n" +
            "                \"minScoreAtLowerBoundary\": 20,\n" +
            "                \"minScoreAtUpperBoundary\": 20,\n" +
            "                \"percentageLowerBoundaryExactMatch\": 0,\n" +
            "                \"percentageUpperBoundaryExactMatch\": 0,\n" +
            "                \"maxScoreForExactMatch\": 40,\n" +
            "                \"additionalScoreForExactMatch\": 50\n" +
            "            },\n" +
            "            \"filter\": {\n" +
            "                \"percentageLowerBoundary\": 20,\n" +
            "                \"percentageUpperBoundary\": 10\n" +
            "            }\n" +
            "        }\n" +
            "    ]\n" +
            "}";


    @Test
    public void testRounding()  {

        final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.of(
                ESJavaClientConverterConfig.builder()
                        .rawQueryInputType(ESJavaClientConverterConfig.RawQueryInputType.JSON)
                        .build()
        );

        final QuerqyConfig querqyConfig = QuerqyConfig.builder()

                .numberUnitRules(NumberUnitRulesDefinition.builder()
                        .rewriterId("id2")
                        .rules(CONFIG_ROUNDING)
                        .numberUnitQueryCreator(new NumberUnitQueryCreatorElasticsearch(DEFAULT_SCALE_FOR_LINEAR_FUNCTIONS))
                        .build())
                .build();

        final QueryConfig queryConfig = QueryConfig.builder()
                .field("name", 40.0f)
                .field("type", 20.0f)
                .minimumShouldMatch("100%")
                .tie(0.0f)
                .build();

        final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                .querqyConfig(querqyConfig)
                .queryConfig(queryConfig)
                .converterFactory(converterFactory)
                .build();


        final RewrittenQuery<Query> query = queryRewriting.rewriteQuery("6.9IN bag");
        System.out.println(query);
        final Query convertedQuery = query.getConvertedQuery();
        System.out.println(convertedQuery);
    }

}
