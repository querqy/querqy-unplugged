package querqy.converter.generic.builder;

import querqy.model.RawQuery;

public interface RawQueryBuilder<T> {

    T build(RawQuery rawQuery);

}
