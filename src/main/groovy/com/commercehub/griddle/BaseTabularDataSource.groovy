package com.commercehub.griddle

abstract class BaseTabularDataSource implements TabularDataSource {

    protected Closure<String> columnNameTransformer = Transformers.noop
    protected Closure<String> valueTransformer = Transformers.noop

    @Override
    void setColumnNameTransformer(Closure<String> columnNameTransformer) {
        this.columnNameTransformer = columnNameTransformer
    }

    @Override
    void setValueTransformer(Closure<String> valueTransformer) {
        this.valueTransformer = valueTransformer
    }

}
