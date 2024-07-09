package querqy.rewriter.graph.traversal;

import querqy.rewriter.graph.Edge;

import java.util.List;

public class StateExchangingGraphTraversal<T> {

    private final List<Edge> startingEdges;
    private final TraversalState<T> traversalState;

    private StateExchangingGraphTraversal(final List<Edge> startingEdges) {
        this.startingEdges = startingEdges;
        this.traversalState = TraversalState.of();
    }

    public TraversalState<T> next() {
        if (!traversalState.isFinished()) {
            traverseToNextActive();
        }

        return traversalState;
    }

    public void traverseToNextActive() {
        traverseToNext();

        if (!traversalState.isEmpty()) {
            routeTraversalByEdgeLabel();
        }
    }

    public void traverseToNext() {
        if (!traversalState.isInitialized()) {
            initialize();

        } else if (traversalState.hasExchangedState() && traversalState.currentStep().hasChildren()) {
            nextChild();

        } else if (traversalState.currentStep().hasNextSibling()) {
            nextSibling();

        } else {
            backToParent();
        }
    }

    public void initialize() {
        final TraversalStep<T> initialStep = TraversalStep.of(startingEdges);
        traversalState.initialize(initialStep);
    }

    private void nextChild() {
        final List<Edge> children = traversalState.currentStep().getChildren();
        final TraversalStep<T> nextStep = TraversalStep.of(children);
        traversalState.addStep(nextStep);
    }

    private void nextSibling() {
        traversalState.currentStep().setNextSibling();
    }

    private void backToParent() {
        traversalState.removeLastStep();
        next();
    }

    private void routeTraversalByEdgeLabel() {
        if (traversalState.currentStep().isDeleted()) {
            skipDeletedEdge();
        }
    }

    private void skipDeletedEdge() {
        // add children of deleted edge as siblings to deleted edge
        // but only if the deleted edge is not a root edge in the current traversal (= needs to be a subsequent lookup)
        // otherwise the subsequent edge is a duplicate beginning edge
        // given A -> B -> C
        // if B is deleted, we want to add C as sibling of A, but only if C is a subsequent lookup for A
        // otherwise we would add C as sibling of A, which is a duplicate, as C is already considered as a starting edge

        if (!traversalState.isRoot() && traversalState.currentStep().hasChildren()) {
            final List<Edge> childrenOfDeleted = traversalState.currentStep().getChildren();
            traversalState.currentStep().addSiblings(childrenOfDeleted);
        }

        next();
    }

    public static <T> StateExchangingGraphTraversal<T> of(final List<Edge> startingEdges) {
        return new StateExchangingGraphTraversal<>(startingEdges);
    }
}
