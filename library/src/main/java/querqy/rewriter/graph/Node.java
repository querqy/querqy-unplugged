package querqy.rewriter.graph;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class Node {

    private final List<Edge> targetEdges;

    public void addEdge(final Edge edge) {
        targetEdges.add(edge);
    }

    public static Node of() {
        return new Node(new ArrayList<>());
    }

    public static Node of(final Edge edge) {
        final ArrayList<Edge> targetEdges = new ArrayList<>();
        targetEdges.add(edge);
        return new Node(targetEdges);
    }
}
