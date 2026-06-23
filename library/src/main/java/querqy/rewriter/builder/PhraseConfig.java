package querqy.rewriter.builder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PhraseConfig {

    @NonNull @Singular private List<FieldBoost> fields;
    @Builder.Default private int slop = 0;
}
