package querqy.rewriter.graph.modification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import querqy.rewriter.graph.Edge;

import java.util.List;

@RequiredArgsConstructor(staticName = "of")
@Getter
public class GraphExpansion {

    private final List<Edge> newEdges;
    private final List<Edge> targetEdges;

}
