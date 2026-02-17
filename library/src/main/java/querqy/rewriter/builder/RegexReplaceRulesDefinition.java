package querqy.rewriter.builder;


import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class RegexReplaceRulesDefinition {

    @JsonAlias("id") @NonNull
    private String rewriterId;
    @NonNull private String rules;

    @Builder.Default private boolean ignoreCase = true;
}
