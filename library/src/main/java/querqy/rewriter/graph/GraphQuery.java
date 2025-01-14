package querqy.rewriter.graph;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

// Dictionary -> interface for lookups
// LuceneDictionary -> implementation of Dictionary
// Implement DictionaryLookup -> takes a list of query terms and applies stateful lookup


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GraphQuery {

    private static final Pattern WHITESPACE_SPLIT = Pattern.compile("\\s+");

    private final Node rootNode = Node.of();
    private final List<Edge> edges = new ArrayList<>();

    private void initiateGraph(final List<String> queryTerms) {
        addSubGraph(SubGraph.of(rootNode, Node.of(), queryTerms));
    }

    public void addSubGraph(final SubGraph subGraph) {
        final List<Node> nodes = createNodeList(subGraph);

        for (int i = 0; i < nodes.size() - 1; i++) {
            final Edge edge = Edge.of(nodes.get(i), subGraph.get(i), nodes.get(i + 1));
            nodes.get(i).addEdge(edge);
            edges.add(edge);
        }
    }

    private List<Node> createNodeList(final SubGraph subGraph) {
        final List<Node> nodes = new ArrayList<>(subGraph.size() + 1);
        nodes.add(subGraph.getStartNode());

        for (int i = 0; i < subGraph.size() - 1; i++) {
            nodes.add(Node.of());
        }

        nodes.add(subGraph.getEndNode());
        return nodes;
    }

    public List<Edge> getRootEdges() {
        return rootNode.getTargetEdges();
    }

    public static GraphQuery of(final String queryInput) {
        final List<String> queryTerms = Arrays.asList(queryInput.split(WHITESPACE_SPLIT.pattern()));
        return of(queryTerms);
    }

    public static GraphQuery of(final String... queryTerms) {
        return of(Arrays.asList(queryTerms));
    }

     public static GraphQuery of(final List<String> queryTerms) {
        final GraphQuery graphQuery = new GraphQuery();
        graphQuery.initiateGraph(queryTerms);
        return graphQuery;
    }

    @RequiredArgsConstructor(staticName = "of")
    @Getter
    public static class SubGraph {

        private final Node startNode;
        private final Node endNode;
        private final List<String> terms;

        public int size() {
            return terms.size();
        }

        public String get(final int index) {
            return terms.get(index);
        }

    }
}