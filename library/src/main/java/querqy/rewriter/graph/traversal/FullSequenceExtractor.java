package querqy.rewriter.graph.traversal;

import lombok.RequiredArgsConstructor;
import querqy.rewriter.graph.GraphQuery;

@RequiredArgsConstructor(staticName = "of")
public class FullSequenceExtractor {

    private final GraphQuery graphQuery;

    // This is not injected as the logic of this class is deeply tied to how the traversal is done, so I prefer here to test them combined
//    private final StateExchangingGraphTraversal<String> traversal = StateExchangingGraphTraversal.of(graphQuery.getEdges());

}
