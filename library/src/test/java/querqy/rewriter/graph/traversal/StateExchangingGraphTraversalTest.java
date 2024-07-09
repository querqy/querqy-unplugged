package querqy.rewriter.graph.traversal;

import org.junit.Ignore;
import org.junit.Test;
import querqy.rewriter.graph.GraphQuery;
import querqy.rewriter.graph.GraphTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StateExchangingGraphTraversalTest {

    @Test
    public void testThat_allSubsequencesAreReturned_forAlwaysExchangingState() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c")
                .addSubGraph("a", "b", "d", "e")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());
        final List<String> sequences = extractAllSubsequences(traversal);

        assertThat(sequences).containsExactly("a", "a b", "a b c", "b", "b c", "c", "d", "d e", "d e c", "e", "e c");
    }

    @Test
    public void testThat_traversalVisitsOnlySingleEdges_forNoStateReturned() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c")
                .addSubGraph("a", "b", "d", "e")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());

        assert traversal.next().currentStep().getEdge().getTerm().equals("a");
        assert traversal.next().currentStep().getEdge().getTerm().equals("b");
        assert traversal.next().currentStep().getEdge().getTerm().equals("c");
        assert traversal.next().currentStep().getEdge().getTerm().equals("d");
        assert traversal.next().currentStep().getEdge().getTerm().equals("e");
    }

    @Test
    public void testThat_traversalVisitsFirstTwoEdges_forStateReturnedOncePerStartingEdge() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c")
                .addSubGraph("a", "b", "d", "e")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());

        traversal.next().setExchangedState("");  // set state for a
        assertThat(traversal.next().getTerms()).containsExactly("a", "b");  // do not set state for a b

        traversal.next().setExchangedState("");  // set state for b
        assertThat(traversal.next().getTerms()).containsExactly("b", "c");  // do not set state for b c

        traversal.next();  // do not set state for c
        traversal.next().setExchangedState("");  // set state for d
        assertThat(traversal.next().getTerms()).containsExactly("d", "e");  // do not set state for d e

        traversal.next().setExchangedState("");  // set state for e
        assertThat(traversal.next().getTerms()).containsExactly("e", "c");  // do not set state for e c

        assertThat(traversal.next().isFinished()).isTrue();
    }

    @Test
    public void testThat_termIsSkippedInMidOfSimpleSequence_ifEdgeIsLabeledAsDeleted() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c")
                .delete("b")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());
        final List<String> sequences = extractAllSubsequences(traversal);

        assertThat(sequences).containsExactly("a", "a c", "c");
    }

    @Test
    public void testThat_termIsSkippedAtBeginningOfSimpleSequence_ifEdgeIsLabeledAsDeleted() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c")
                .delete("a")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());
        final List<String> sequences = extractAllSubsequences(traversal);

        assertThat(sequences).containsExactly("b", "b c", "c");
    }

    @Test
    public void testThat_termIsSkippedAtEndOfSimpleSequence_ifEdgeIsLabeledAsDeleted() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c")
                .delete("c")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());
        final List<String> sequences = extractAllSubsequences(traversal);

        assertThat(sequences).containsExactly("a", "a b", "b");
    }

    @Test
    public void testThat_termIsSkippedInMidOfSimpleSequence_ifTwoEdgesAreLabeledAsDeleted() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c", "d")
                .delete("b")
                .delete("c")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());
        final List<String> sequences = extractAllSubsequences(traversal);

        assertThat(sequences).containsExactly("a", "a d", "d");
    }

    @Test
    public void testThat_termIsSkippedAtBeginningOfSimpleSequence_ifTwoEdgesAreLabeledAsDeleted() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c", "d")
                .delete("a")
                .delete("b")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());
        final List<String> sequences = extractAllSubsequences(traversal);

        assertThat(sequences).containsExactly("c", "c d", "d");
    }

    @Test
    public void testThat_termIsSkippedAtEndOfSimpleSequence_ifTwoEdgesAreLabeledAsDeleted() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c", "d")
                .delete("c")
                .delete("d")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());
        final List<String> sequences = extractAllSubsequences(traversal);

        assertThat(sequences).containsExactly("a", "a b", "b");
    }

    @Test
    public void testThat_termIsSkippedInNestedSequence_ifEdgeIsLabeledAsDeleted() {
        // O - a - O - b - O
        //  \     /       /
        //   c  -d-      /
        //    \ /       /
        //     O   -   e

        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b")
                .addSubGraph("a", "a", "c", "d")
                .addSubGraph("d", "b", "e")
                .delete("d")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());
        final List<String> sequences = extractAllSubsequences(traversal);

        assertThat(sequences).containsExactlyInAnyOrder("a", "a b", "b", "c", "c b", "c e", "e");
    }

    @Test
    public void testThat_termsAreSkippedInNestedSequence_ifEdgeIsLabeledAsDeleted() {
        // O - a - O - -b- - O
        //  \     /         /
        //   c  -d-        /
        //    \ /         /
        //     O    -    e

        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b")
                .addSubGraph("a", "a", "c", "d")
                .addSubGraph("d", "b", "e")
                .delete("d")
                .delete("b")
                .build();

        final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());
        final List<String> sequences = extractAllSubsequences(traversal);

        assertThat(sequences).containsExactlyInAnyOrder("a", "c", "c e", "e");
    }

    private List<String> extractAllSubsequences(final StateExchangingGraphTraversal<String> traversal) {
        final List<String> sequences = new ArrayList<>();
        TraversalState<String> state = traversal.next();
        state.setExchangedState(String.join(" ", state.getTerms()));

        while (!state.isFinished()) {
            sequences.add(String.join(" ", state.getTerms()));
            state.setExchangedState(String.join(" ", state.getTerms()));
            traversal.next();
        }

        return sequences;
    }
}
