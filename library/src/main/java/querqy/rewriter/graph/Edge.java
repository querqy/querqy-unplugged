package querqy.rewriter.graph;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(staticName = "of")
@Getter
public class Edge {

    public enum Label {
        ACTIVE,
        DELETED,
        STOP_WORD,
        RELAXED
    }

    private final Node sourceNode;
    private final String term;
    private final Node targetNode;

    @Setter
    private Label label = Label.ACTIVE;

    public String toString() {
        if (label == Label.DELETED) {
            return "-" + term + "-";
        }
        
        return term;
    }

}
