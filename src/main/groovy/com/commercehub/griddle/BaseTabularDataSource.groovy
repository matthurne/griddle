package com.commercehub.griddle

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

abstract class BaseTabularDataSource implements TabularDataSource {

    protected Closure<String> columnNameTransformer = Transformers.noop
    protected Closure<String> valueTransformer = Transformers.noop

    @Override
    void setColumnNameTransformer(@ClosureParams(value=SimpleType, options="java.lang.String") Closure<String> columnNameTransformer) {
        this.columnNameTransformer = columnNameTransformer
    }

    @Override
    void setValueTransformer(@ClosureParams(value=SimpleType, options="java.lang.String") Closure<String> valueTransformer) {
        this.valueTransformer = valueTransformer
    }

}
