package querqy.rewriter.graph.traversal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "of")
public class TraversalState<T> {

    @Getter private final Deque<TraversalStep<T>> traversalSteps = new LinkedList<>();

    private boolean isInitialized = false;

    public void initialize(final TraversalStep<T> traversalStep) {
        traversalSteps.addLast(traversalStep);
        isInitialized = true;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isEmpty() {
        return traversalSteps.isEmpty();
    }

    public int size() {
        return traversalSteps.size();
    }

    public boolean isRoot() {
        return size() == 1;
    }

    public boolean isFinished() {
        return isInitialized() && isEmpty();
    }

    public TraversalStep<T> currentStep() {
        assertActive();
        return traversalSteps.getLast();
    }

    public void addStep(final TraversalStep<T> step) {
        traversalSteps.addLast(step);
    }

    public void removeLastStep() {
        assertActive();
        traversalSteps.removeLast();
    }

    public void assertActive() {
        if (!isInitialized || isFinished()) {
            throw new IllegalStateException("Traversal has not been initialized or is already finished");

        } else if (traversalSteps.size() < 1) {
            throw new IllegalStateException("No edges are available");
        }
    }

    // TODO: rename to CurrentState
    // TODO: need good name for something like getLastExchangedState
    public boolean hasExchangedState() {
        return size() > 0 && traversalSteps.getLast().hasState();
    }

    public Optional<T> getExchangedState() {
        if (isEmpty()) {
            return Optional.empty();

        } else {
            return traversalSteps.getLast().getState();
        }
    }

    public void setExchangedState(final T state) {
        assertActive();
        traversalSteps.getLast().setState(state);
    }

    public List<String> getTerms() {
        return traversalSteps.stream().map(TraversalStep::getTerm).collect(Collectors.toList());
    }

    public String toString() {
        return traversalSteps.stream().map(TraversalStep::toString).collect(Collectors.joining(" -> "));
    }
}
