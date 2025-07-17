package elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterFactory;
import querqy.converter.elasticsearch.javaclient.NumberUnitQueryCreatorElasticsearch;
import querqy.domain.RewrittenQuery;
import querqy.rewrite.contrib.NumberUnitRewriterFactory;
import querqy.rewrite.contrib.numberunit.model.FieldDefinition;
import querqy.rewrite.contrib.numberunit.model.NumberUnitDefinition;
import querqy.rewrite.contrib.numberunit.model.UnitDefinition;
import querqy.rewriter.builder.CommonRulesDefinition;
import querqy.rewriter.builder.NumberUnitConfigObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class QuerqyConfigTest {

    private static final String EXCEPTION_MESSAGE = "NumberUnitRewriter not properly configured. " +
            "At least one unit and one field need to be properly defined, e. g. \n" +
            "{\n" +
            "  \"numberUnitDefinitions\": [\n" +
            "    {\n" +
            "      \"units\": [ { \"term\": \"cm\" } ],\n" +
            "      \"fields\": [ { \"fieldName\": \"weight\" } ]\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";

    private static final int DEFAULT_UNIT_MULTIPLIER = 1;

    private static final int DEFAULT_SCALE_FOR_LINEAR_FUNCTIONS = 5;
    private static final int DEFAULT_FIELD_SCALE = 0;

    private static final float DEFAULT_BOOST_MAX_SCORE_FOR_EXACT_MATCH = 200;
    private static final float DEFAULT_BOOST_MIN_SCORE_AT_UPPER_BOUNDARY = 100;
    private static final float DEFAULT_BOOST_MIN_SCORE_AT_LOWER_BOUNDARY = 100;
    private static final float DEFAULT_BOOST_ADDITIONAL_SCORE_FOR_EXACT_MATCH = 100;

    private static final float DEFAULT_BOOST_PERCENTAGE_UPPER_BOUNDARY = 20;
    private static final float DEFAULT_BOOST_PERCENTAGE_LOWER_BOUNDARY = 20;
    private static final float DEFAULT_BOOST_PERCENTAGE_UPPER_BOUNDARY_EXACT_MATCH = 0;
    private static final float DEFAULT_BOOST_PERCENTAGE_LOWER_BOUNDARY_EXACT_MATCH = 0;

    private static final float DEFAULT_FILTER_PERCENTAGE_LOWER_BOUNDARY = 20;
    private static final float DEFAULT_FILTER_PERCENTAGE_UPPER_BOUNDARY = 20;


    static final String NUMBER_UNIT_CONFIG = "{\n" +
            "   \"numberUnitDefinitions\": [\n" +
            "       {\n" +
            "          \"units\": [ { \"term\": \"inch\" } ],\n" +
            "          \"fields\": [ { \"fieldName\": \"screen_size\" } ],\n" +
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

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

        }
        return true;
    }

    private List<UnitDefinition> parseUnitDefinitions(final NumberUnitConfigObject.NumberUnitDefinitionObject numberUnitDefinitionObject) {
        final List<NumberUnitConfigObject.UnitObject> unitObjects = numberUnitDefinitionObject.getUnits();
        if (unitObjects == null || unitObjects.isEmpty()) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }

        return unitObjects.stream()
                .peek(unitObject -> {
                    if (isBlank(unitObject.getTerm())) {
                        throw new IllegalArgumentException("Unit definition requires a term to be defined");
                    }})
                .map(unitObject -> new UnitDefinition(
                        unitObject.getTerm(),
                        getOrDefaultBigDecimalForFloat(unitObject::getMultiplier, DEFAULT_UNIT_MULTIPLIER)))
                .collect(Collectors.toList());
    }

    private List<FieldDefinition> parseFieldDefinitions(final NumberUnitConfigObject.NumberUnitDefinitionObject numberUnitDefinitionObject) {
        final List<NumberUnitConfigObject.FieldObject> fieldObjects = numberUnitDefinitionObject.getFields();
        if (fieldObjects == null || fieldObjects.isEmpty()) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }

        return fieldObjects.stream()
                .peek(fieldObject -> {
                    if (isBlank(fieldObject.getFieldName())) {
                        throw new IllegalArgumentException("Unit definition requires a term to be defined");
                    }})
                .map(fieldObject -> new FieldDefinition(
                        fieldObject.getFieldName(),
                        getOrDefaultInt(fieldObject::getScale, DEFAULT_FIELD_SCALE)))
                .collect(Collectors.toList());
    }

    private BigDecimal getOrDefaultBigDecimalForFloat(final Supplier<Float> supplier, final float defaultValue) {
        final Float value = supplier.get();
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.valueOf(defaultValue);
    }

    private int getOrDefaultInt(final Supplier<Integer> supplier, final int defaultValue) {
        final Integer value = supplier.get();
        return value != null ? value : defaultValue;
    }
    private NumberUnitDefinition parseNumberUnitDefinition(final NumberUnitConfigObject.NumberUnitDefinitionObject defObj) {

        final NumberUnitDefinition.Builder builder = NumberUnitDefinition.builder()
                .addUnits(this.parseUnitDefinitions(defObj))
                .addFields(this.parseFieldDefinitions(defObj));

        final NumberUnitConfigObject.BoostObject boost = defObj.getBoost() != null
                ? defObj.getBoost()
                : new NumberUnitConfigObject.BoostObject();

        builder
                .setMaxScoreForExactMatch(getOrDefaultBigDecimalForFloat(
                        boost::getMaxScoreForExactMatch, DEFAULT_BOOST_MAX_SCORE_FOR_EXACT_MATCH))
                .setMinScoreAtUpperBoundary(getOrDefaultBigDecimalForFloat(
                        boost::getMinScoreAtUpperBoundary, DEFAULT_BOOST_MIN_SCORE_AT_UPPER_BOUNDARY))
                .setMinScoreAtLowerBoundary(getOrDefaultBigDecimalForFloat(
                        boost::getMinScoreAtLowerBoundary, DEFAULT_BOOST_MIN_SCORE_AT_LOWER_BOUNDARY))
                .setAdditionalScoreForExactMatch(getOrDefaultBigDecimalForFloat(
                        boost::getAdditionalScoreForExactMatch, DEFAULT_BOOST_ADDITIONAL_SCORE_FOR_EXACT_MATCH))
                .setBoostPercentageUpperBoundary(getOrDefaultBigDecimalForFloat(
                        boost::getPercentageUpperBoundary, DEFAULT_BOOST_PERCENTAGE_UPPER_BOUNDARY))
                .setBoostPercentageLowerBoundary(getOrDefaultBigDecimalForFloat(
                        boost::getPercentageLowerBoundary, DEFAULT_BOOST_PERCENTAGE_LOWER_BOUNDARY))
                .setBoostPercentageUpperBoundaryExactMatch(getOrDefaultBigDecimalForFloat(
                        boost::getPercentageUpperBoundaryExactMatch, DEFAULT_BOOST_PERCENTAGE_UPPER_BOUNDARY_EXACT_MATCH))
                .setBoostPercentageLowerBoundaryExactMatch(getOrDefaultBigDecimalForFloat(
                        boost::getPercentageLowerBoundaryExactMatch, DEFAULT_BOOST_PERCENTAGE_LOWER_BOUNDARY_EXACT_MATCH));

        final NumberUnitConfigObject.FilterObject filter = defObj.getFilter() != null
                ? defObj.getFilter()
                : new NumberUnitConfigObject.FilterObject();

        builder
                .setFilterPercentageUpperBoundary(getOrDefaultBigDecimalForFloat(
                        filter::getPercentageUpperBoundary, DEFAULT_FILTER_PERCENTAGE_UPPER_BOUNDARY))
                .setFilterPercentageLowerBoundary(getOrDefaultBigDecimalForFloat(
                        filter::getPercentageLowerBoundary, DEFAULT_FILTER_PERCENTAGE_LOWER_BOUNDARY));

        return builder.build();
    }

    protected List<NumberUnitDefinition> getNumberUnitConfig() throws IOException {

        final NumberUnitConfigObject numberUnitConfigObject = new ObjectMapper().readValue(
                NUMBER_UNIT_CONFIG, NumberUnitConfigObject.class);

        final List<NumberUnitConfigObject.NumberUnitDefinitionObject> numberUnitDefinitionObjects =
                numberUnitConfigObject.getNumberUnitDefinitions();
        return numberUnitDefinitionObjects.stream().map(this::parseNumberUnitDefinition).collect(Collectors.toList());


    }

    @Test
    public void testCommonRules() throws IOException {

        final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.create();

        final QuerqyConfig querqyConfig = QuerqyConfig.builder()

                .commonRules(
                        CommonRulesDefinition.builder()
                                .rewriterId("id1")
                                .rules("iphone => \n SYNONYM: apple smartphone")
                                .build()
                )
                .rewriterFactory(
                        new NumberUnitRewriterFactory("id2", getNumberUnitConfig(), new NumberUnitQueryCreatorElasticsearch(DEFAULT_SCALE_FOR_LINEAR_FUNCTIONS))
                )
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


        final RewrittenQuery<Query> query = queryRewriting.rewriteQuery("iphone 4 inch");
        System.out.println(query);
        final Query convertedQuery = query.getConvertedQuery();
        //System.out.println(convertedQuery.getClass());
        System.out.println(convertedQuery);
    }

    @Test
    public void testNumberUnitRewriter() {

    /*    final NumberUnitConfigObject numberUnitConfigObject;
        try {
            numberUnitConfigObject = new ObjectMapper().readValue(
                    (String) numberUnitConfig, NumberUnitConfigObject.class);
        } catch (IOException e) {
            // checked in this::validateConfiguration
            return;
        }

        final int scale = getOrDefaultInt(numberUnitConfigObject::getScaleForLinearFunctions,
                DEFAULT_SCALE_FOR_LINEAR_FUNCTIONS);

        this.delegate = new querqy.rewrite.contrib.NumberUnitRewriterFactory(
                rewriterId, parseConfig(numberUnitConfigObject), new NumberUnitQueryCreatorElasticsearch(scale));

        final QuerqyConfig config = QuerqyConfig.builder()
                .rewriterFactory(new NumberUnitRewriterFactory("id1", ))

                .build();

        final RewriteChain rewriteChain = config.getRewriteChain();
        System.out.println(rewriteChain);*/
    }
}
