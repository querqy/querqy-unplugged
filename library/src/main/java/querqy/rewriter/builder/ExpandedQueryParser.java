package querqy.rewriter.builder;

import querqy.model.ExpandedQuery;
import querqy.model.MatchAllQuery;
import querqy.model.Query;
import querqy.parser.QuerqyParser;
import querqy.rewriter.commonrules.QuerqyParserFactory;

import java.util.Set;

public class ExpandedQueryParser {
    public static final String MATCH_ALL_FIELD_VALUE_INPUT = "*:*";
    public static final String MATCH_ALL_WILDCARD_INPUT = "*";

    private final Set<String> matchAllQueryInputs;

    private ExpandedQueryParser(final Set<String> matchAllQueryInputs) {
        this.matchAllQueryInputs = matchAllQueryInputs;
    }

    public static ExpandedQueryParser create() {
        return new ExpandedQueryParser(Set.of());
    }

    public static ExpandedQueryParser of(final Set<String> matchAllQueryInputs) {
        return new ExpandedQueryParser(matchAllQueryInputs);
    }

    public ExpandedQuery parseQuery(final QuerqyParserFactory parserFactory, final String queryInput) {
        if (matchAllQueryInputs.contains(queryInput)) {
            return new ExpandedQuery(new MatchAllQuery());

        } else {
            final QuerqyParser parser = parserFactory.createParser();

            final Query parsedQuery = parser.parse(queryInput);
            return new ExpandedQuery(parsedQuery);
        }
    }

}