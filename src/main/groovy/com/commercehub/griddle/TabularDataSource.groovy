package com.commercehub.griddle

interface TabularDataSource {

    /**
     * Opens a file and allows processing it within a closure.  The closure is passed an Iterable<TabularData>.
     */
    void withFile(File file, Closure tableHandler)

    /**
     * Sets the column name transformation to apply during all subsequent file processing.  The closure is passed
     * the raw String value and should return the transformed value.  By default, a no-op transformation is used.
     */
    void setColumnNameTransformer(Closure<String> columnNameTransformer)

    /**
     * Sets the data cell value transformation to apply during all subsequent file processing.  The closure is passed
     * the raw String value and should return the transformed value.  By default, a no-op transformation is used.
     */
    void setValueTransformer(Closure<String> valueTransformer)

}