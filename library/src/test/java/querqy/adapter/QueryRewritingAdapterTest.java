package querqy.adapter;

import org.junit.Test;
import querqy.QueryRewritingConfig;
import querqy.adapter.rewriter.builder.CommonRulesRewriterFactoryCreator;
import querqy.model.convert.builder.ExpandedQueryBuilder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.DisjunctionMaxQueryBuilder.dmq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;
import static querqy.model.convert.builder.TermBuilder.term;

public class QueryRewritingAdapterTest {

    @Test
    public void testThat_synonymsAreApplied_forGivenCommonRulesRewriter() throws IOException {
        final QueryRewritingConfig rewritingConfig = QueryRewritingConfig.builder()
                .rewriterFactory(
                        CommonRulesRewriterFactoryCreator.creator()
                                .rewriterId("1")
                                .rules("apple smartphone =>\n  SYNONYM: iphone")
                                .createFactory()
                )
                .build();

        final QueryRewritingAdapter adapter = QueryRewritingAdapter.builder()
                .queryInput("apple smartphone")
                .queryRewritingConfig(rewritingConfig)
                .build();

        final ExpandedQueryBuilder expanded = expanded(adapter.rewriteQuery().getQuery());

        assertThat(expanded.getUserQuery()).isEqualTo(
                bq(
                        dmq(
                                term("apple"),
                                term("iphone", true)
                        ),
                        dmq(
                                term("smartphone"),
                                term("iphone", true)
                        )
                )
        );
    }

}
