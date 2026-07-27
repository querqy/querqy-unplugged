package querqy.rewriter.builder;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import querqy.rewriter.numberunit.NumberUnitQueryCreator;

import java.util.function.IntFunction;

@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"numberUnitQueryCreator"})
@ToString
public class NumberUnitRulesDefinition {
    @JsonAlias("id") @NonNull
    private String rewriterId;

    @NonNull private final String rules;
    @NonNull private final IntFunction<NumberUnitQueryCreator> numberUnitQueryCreator;

}