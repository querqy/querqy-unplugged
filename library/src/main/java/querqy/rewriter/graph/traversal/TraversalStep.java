package querqy.rewriter.graph.traversal;

import lombok.Setter;
import querqy.rewriter.graph.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TraversalStep<T> {
    private final List<Edge> edges;

    @Setter private T state;
    private Edge currentEdge;

    private int nextSiblingIndex;
    private boolean hasReturnedChildren;

    private TraversalStep(final List<Edge> edges, final T state) {
        if (edges == null || edges.isEmpty()) {
            throw new IllegalArgumentException("Edges must not be null or empty");
        }

        this.edges = new ArrayList<>(edges);
        this.state = state;
        currentEdge = edges.get(0);
        nextSiblingIndex = 1;
        hasReturnedChildren = false;
    }

    public Edge getEdge() {
        return currentEdge;
    }

    public boolean hasChildren() {
        return currentEdge != null && getChildrenStateless().size() > 0 && !hasReturnedChildren;
    }

    public List<Edge> getChildren() {
        hasReturnedChildren = true;
        return getChildrenStateless();
    }

    private List<Edge> getChildrenStateless() {
        return getEdge().getTargetNode().getTargetEdges();
    }

    public boolean hasNextSibling() {
        return nextSiblingIndex < edges.size();
    }

    public void setNextSibling() {
        hasReturnedChildren = false;
        state = null;
        currentEdge = edges.get(nextSiblingIndex++);
    }

    public void addSiblings(final List<Edge> edges) {
        this.edges.addAll(edges);
    }

    public boolean isDeleted() {
        return currentEdge.getLabel() == Edge.Label.DELETED;
    }

    public boolean hasState() {
        return state != null;
    }

    // TODO: Optional
    public Optional<T> getState() {
        return Optional.ofNullable(state);
    }

    public String getTerm() {
        return currentEdge.getTerm();
    }

    public String toString() {
        return "Step(edge=" + currentEdge.toString() + ", state=" + state + ")";
    }

    public static <T> TraversalStep<T> of(final List<Edge> edges) {
        return new TraversalStep<>(edges, null);
    }

    @Deprecated
    public static <T> TraversalStep<T> of(final List<Edge> edges, final T state) {
        return new TraversalStep<>(edges, state);
    }
}
