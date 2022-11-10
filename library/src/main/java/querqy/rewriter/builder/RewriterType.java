package querqy.rewriter.builder;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum RewriterType {
    COMMON_RULES("common"),
    REPLACE_RULES("replace");

    private static final Map<String, RewriterType> TYPE_MAPPING = Arrays.stream(RewriterType.values())
            .collect(
                    Collectors.toMap(type -> type.name, type -> type)
            );

    private final String name;

    RewriterType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<RewriterType> fromString(final String name) {
        return Optional.ofNullable(
                TYPE_MAPPING.get(name)
        );
    }
}
