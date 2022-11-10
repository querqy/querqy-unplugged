package querqy.rewriter.builder;

import querqy.rewrite.RewriterFactory;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RewriterSupport {

    public static RewriterFactory createRewriterFactory(final String type, final String... args) {
        final Map<String, String> argsAsMap = argsToMap(args);
        return createRewriterFactory(type, argsAsMap);
    }

    public static RewriterFactory createRewriterFactory(
            final String type, final Map<String, String> attributes, final String... args) {

        final Map<String, String> argsAsMap = argsToMap(args);
        final Map<String, String> mergedMaps = mergeMaps(attributes, argsAsMap);
        return createRewriterFactory(type, mergedMaps);
    }

    private static Map<String, String> argsToMap(final String... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Length of array for RewriterFactory must be even, but was odd");
        }

        return IntStream.iterate(0, i -> i < args.length, i -> i + 2)
                .boxed()
                .collect(
                        Collectors.toMap(i -> args[i], i -> args[i + 1])
                );
    }

    private static Map<String, String> mergeMaps(final Map<String, String> map1, final Map<String, String> map2) {
        return Stream.of(map1, map2)
                .flatMap(map -> map.entrySet().stream())
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (v1, v2) -> v2
                        )
                );

    }

    public static RewriterFactory createRewriterFactory(final String type, final Map<String, String> attributes) {
        final RewriterType rewriterType = RewriterType.fromString(type)
                .orElseThrow(() -> new UnsupportedOperationException(
                        "Rewriter type " + type + " is not supported by RewriterSupport"));

        return RewriterFactoryBuilder.of(rewriterType, attributes).build();
    }


}
