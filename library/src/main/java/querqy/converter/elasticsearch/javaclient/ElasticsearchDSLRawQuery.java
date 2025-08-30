package querqy.converter.elasticsearch.javaclient;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import querqy.model.BooleanParent;
import querqy.model.QuerqyQuery;
import querqy.model.RawQuery;

public class ElasticsearchDSLRawQuery extends RawQuery {

    final Query query;

    public ElasticsearchDSLRawQuery(final BooleanParent parent, final Query query, final Occur occur,
                                    final boolean isGenerated) {
        super(parent, occur, isGenerated);
        this.query = query;
    }

    @Override
    public QuerqyQuery<BooleanParent> clone(final BooleanParent newParent) {
        return clone(newParent, this.generated);
    }

    @Override
    public QuerqyQuery<BooleanParent> clone(final BooleanParent newParent, final boolean generated) {
        return new ElasticsearchDSLRawQuery(newParent, query, occur, generated);
    }

    public Query getQuery() {
        return query;
    }

}
