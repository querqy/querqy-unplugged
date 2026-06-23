package querqy.rewriter.builder;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class PhraseBoostDefinition {

    @JsonAlias("id") @NonNull
    private String rewriterId;

    private PhraseConfig bigram;
    private PhraseConfig trigram;
    private PhraseConfig full;

    @Builder.Default private float tieBreaker = 0.0f;
}
