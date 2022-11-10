package querqy.rewriter.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonRulesDefinitionTest {

    @Test
    public void testThat_jacksonMapperReturnsTheSameObject_likeLombokBuilder() {
        final String id = "1";
        final String rules = "iphone =>\n  SYNONYM: apple";

        final ObjectMapper mapper = new ObjectMapper();
        final CommonRulesDefinition definitionFromMap = mapper.convertValue(
                Map.of("rewriterId", id, "rules", rules), CommonRulesDefinition.class);

        final CommonRulesDefinition definitionFromBuilder = CommonRulesDefinition.builder()
                .rewriterId(id)
                .rules(rules)
                .build();

        assertThat(definitionFromMap).isEqualTo(definitionFromBuilder);

    }
}
