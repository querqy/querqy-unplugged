package querqy.rewriter.builder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FieldBoost {

    @NonNull private String field;
    @Builder.Default private float boost = 1.0f;
}
