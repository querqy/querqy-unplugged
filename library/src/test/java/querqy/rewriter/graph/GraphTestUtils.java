package querqy.rewriter.graph;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphTestUtils {

    public static Node getNodeByPositions(final Node startNode, final int... positions) {
        return getEdgeByPositions(startNode, positions).getSourceNode();
    }

    public static Edge getEdgeByPositions(final Node startNode, final int... positions) {
        Node node = startNode;
        Edge edge = null;

        for (int position : positions) {
            edge = node.getTargetEdges().get(position);
            node = edge.getTargetNode();
        }

        return edge;
    }

    @RequiredArgsConstructor
    public static class GraphQueryBuilder {

        private final GraphQuery query;


        public static GraphQueryBuilder of(final String... terms) {
            final GraphQuery query = GraphQuery.of(terms);
            return new GraphQueryBuilder(query);
        }

        public GraphQueryBuilder addSubGraph(final String before, final String after, final String... terms) {
            final Map<String, Edge> edgeMapping = getEdgeMapping();

            final Node startNode = edgeMapping.get(before).getSourceNode();
            final Node endNode = edgeMapping.get(after).getTargetNode();

            query.addSubGraph(GraphQuery.SubGraph.of(startNode, endNode, Arrays.asList(terms)));
            return this;
        }

        private Map<String, Edge> getEdgeMapping() {
            return query.getEdges().stream()
                    .collect(Collectors.toMap(Edge::getTerm, edge -> edge));
        }

        public GraphQueryBuilder delete(final String term) {
            final Map<String, Edge> edgeMapping = getEdgeMapping();
            final Edge edge = edgeMapping.get(term);

            edge.setLabel(Edge.Label.DELETED);

            return this;
        }

        public GraphQuery build() {
            return query;
        }
    }

    public static NodePositions node(final int... positions) {
        return new NodePositions(positions);
    }

    @RequiredArgsConstructor
    public static class NodePositions {
        private final int[] positions;
    }
}
