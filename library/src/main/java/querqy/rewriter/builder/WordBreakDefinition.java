package querqy.rewriter.builder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import querqy.rewriter.wordbreak.TermCorpus;

import java.util.List;

@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"termCorpus"})
@ToString(exclude = {"termCorpus"})
public class WordBreakDefinition {

    @NonNull private String rewriterId;
    @NonNull private TermCorpus termCorpus;

    @Builder.Default private boolean lowerCaseInput = false;
    @Builder.Default private int minSuggestionFreq = 1;
    @Builder.Default private int minBreakLength = 3;
    @Builder.Default private List<String> reverseCompoundTriggerWords = List.of();
    @Builder.Default private boolean alwaysAddReverseCompounds = false;
    @Builder.Default private int maxDecompoundExpansions = 3;
    @Builder.Default private boolean verifyDecompoundCollation = false;
    @Builder.Default private List<String> protectedWords = List.of();
    @Builder.Default private String decompoundMorphology = "default";
    @Builder.Default private String compoundMorphology = "default";

}
