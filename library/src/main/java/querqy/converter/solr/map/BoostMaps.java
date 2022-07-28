package querqy.converter.solr.map;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(staticName = "create")
@Getter
public class BoostMaps {

    private final List<Object> boostFunctionQueries = new ArrayList<>();
    private final Map<String, Object> referencedConvertedQueries = new HashMap<>();

    public void addBoostMapTuple(final BoostMapEntry boostMapTuple) {
        boostFunctionQueries.add(boostMapTuple.getBoostFunctionQuery());

        referencedConvertedQueries.put(
                boostMapTuple.getConvertedQueryReference(),
                boostMapTuple.getConvertedQuery()
        );
    }
}
