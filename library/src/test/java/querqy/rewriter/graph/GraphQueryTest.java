package querqy.rewriter.graph;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static querqy.rewriter.graph.GraphTestUtils.getEdgeByPositions;

public class GraphQueryTest {

    @Test
    public void testThat_graphQueryIsCreatedInCorrectOrder() {
        final GraphQuery graphQuery = GraphQuery.of("a", "b", "c");
        final Node startNode = graphQuery.getRootNode();

        assert getEdgeByPositions(startNode, 0).getTerm().equals("a");
        assert getEdgeByPositions(startNode, 0, 0).getTerm().equals("b");
        assert getEdgeByPositions(startNode, 0, 0, 0).getTerm().equals("c");
    }

    @Test
    public void testThat_subGraphsAreFullyConnected() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c")
                .addSubGraph("a", "b", "d", "e")
                .build();

        assert getEdgeByPositions(graphQuery.getRootNode(), 1).getTerm().equals("d");
        assert getEdgeByPositions(graphQuery.getRootNode(), 1, 0).getTerm().equals("e");
        assert getEdgeByPositions(graphQuery.getRootNode(), 1, 0, 0).getTerm().equals("c");
    }

    @Test
    public void testThat_registryContainsAllEdges() {
        final GraphQuery graphQuery = GraphTestUtils.GraphQueryBuilder.of("a", "b", "c")
                .addSubGraph("a", "b", "d", "e")
                .build();

        assert graphQuery.getEdges().size() == 5;

        final List<String> edgeTerms = graphQuery.getEdges().stream().map(Edge::getTerm).collect(Collectors.toList());
        assert edgeTerms.containsAll(List.of("a", "b", "c", "d", "e"));
    }
}
