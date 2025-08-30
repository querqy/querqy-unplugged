package querqy.rewriter.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import querqy.rewrite.contrib.NumberUnitRewriterFactory;
import querqy.rewrite.contrib.numberunit.model.FieldDefinition;
import querqy.rewrite.contrib.numberunit.model.NumberUnitDefinition;
import querqy.rewrite.contrib.numberunit.model.UnitDefinition;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "of")
public class NumberUnitRulesFactorBuilder {

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

    private final NumberUnitRulesDefinition definition;

    public NumberUnitRewriterFactory build() {
        try {
            return new NumberUnitRewriterFactory("id2", getNumberUnitConfig(), definition.getNumberUnitQueryCreator());
        } catch (IOException e) {
            throw new RewriterFactoryBuilderException(e);
        }
    }


    protected List<NumberUnitDefinition> getNumberUnitConfig() throws IOException {

        final NumberUnitConfigObject numberUnitConfigObject = new ObjectMapper().readValue(
                definition.getRules(), NumberUnitConfigObject.class);

        final List<NumberUnitConfigObject.NumberUnitDefinitionObject> numberUnitDefinitionObjects =
                numberUnitConfigObject.getNumberUnitDefinitions();
        return numberUnitDefinitionObjects.stream().map(this::parseNumberUnitDefinition).collect(Collectors.toList());

    }


    private BigDecimal getOrDefaultBigDecimalForFloat(final Supplier<Float> supplier, final float defaultValue) {
        final Float value = supplier.get();
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.valueOf(defaultValue);
    }

    private int getOrDefaultInt(final Supplier<Integer> supplier, final int defaultValue) {
        final Integer value = supplier.get();
        return value != null ? value : defaultValue;
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
                        getOrDefaultBigDecimalForFloat(unitObject::getMultiplier, definition.getDefaultUnitMultiplier())))
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
                        getOrDefaultInt(fieldObject::getScale, definition.getDefaultFieldScale())))
                .collect(Collectors.toList());
    }


    private NumberUnitDefinition parseNumberUnitDefinition(final NumberUnitConfigObject.NumberUnitDefinitionObject defObj) {

        final NumberUnitDefinition.Builder builder = NumberUnitDefinition.builder()
                .addUnits(parseUnitDefinitions(defObj))
                .addFields(parseFieldDefinitions(defObj));

        final NumberUnitConfigObject.BoostObject boost = defObj.getBoost() != null
                ? defObj.getBoost()
                : new NumberUnitConfigObject.BoostObject();

        builder
                .setMaxScoreForExactMatch(getOrDefaultBigDecimalForFloat(
                        boost::getMaxScoreForExactMatch, definition.getDefaultBoostMaxScoreForExactMatch()))
                .setMinScoreAtUpperBoundary(getOrDefaultBigDecimalForFloat(
                        boost::getMinScoreAtUpperBoundary, definition.getDefaultBoostMinScoreAtUpperBoundary()))
                .setMinScoreAtLowerBoundary(getOrDefaultBigDecimalForFloat(
                        boost::getMinScoreAtLowerBoundary, definition.getDefaultBoostMinScoreAtLowerBoundary()))
                .setAdditionalScoreForExactMatch(getOrDefaultBigDecimalForFloat(
                        boost::getAdditionalScoreForExactMatch, definition.getDefaultBoostAdditionalScoreForExactMatch()))
                .setBoostPercentageUpperBoundary(getOrDefaultBigDecimalForFloat(
                        boost::getPercentageUpperBoundary, definition.getDefaultBoostPercentageUpperBoundary()))
                .setBoostPercentageLowerBoundary(getOrDefaultBigDecimalForFloat(
                        boost::getPercentageLowerBoundary, definition.getDefaultBoostPercentageLowerBoundary()))
                .setBoostPercentageUpperBoundaryExactMatch(getOrDefaultBigDecimalForFloat(
                        boost::getPercentageUpperBoundaryExactMatch, definition.getDefaultBoostPercentageUpperBoundaryExactMatch()))
                .setBoostPercentageLowerBoundaryExactMatch(getOrDefaultBigDecimalForFloat(
                        boost::getPercentageLowerBoundaryExactMatch, definition.getDefaultBoostPercentageLowerBoundaryExactMatch()));

        final NumberUnitConfigObject.FilterObject filter = defObj.getFilter() != null
                ? defObj.getFilter()
                : new NumberUnitConfigObject.FilterObject();

        builder
                .setFilterPercentageUpperBoundary(getOrDefaultBigDecimalForFloat(
                        filter::getPercentageUpperBoundary, definition.getDefaultFilterPercentageUpperBoundary()))
                .setFilterPercentageLowerBoundary(getOrDefaultBigDecimalForFloat(
                        filter::getPercentageLowerBoundary, definition.getDefaultFilterPercentageLowerBoundary()));

        return builder.build();
    }

    // TODO: We probably have this somewhere else too?!
    public static boolean isBlank(final CharSequence cs) {
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
}
