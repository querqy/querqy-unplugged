package querqy;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class QueryConfigTest {

    @Test
    public void testThat_exceptionIsThrown_forDuplicateFieldNames() {
        assertThrows(
                IllegalArgumentException.class, () ->
                        QueryConfig.builder()
                                .field("f", 1.0f)
                                .field("f", 1.0f)
                                .build()
        );
    }
}
