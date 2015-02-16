package com.commercehub.griddle

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType

interface TabularDataSource {

    /**
     * Opens a file and allows processing it within a closure.  The closure is passed an Iterable<TabularData>.
     */
    void withFile(File file,
                  @ClosureParams(value=FromString, options="java.lang.Iterable<com.commercehub.griddle.TabularData>") Closure tableHandler)

    /**
     * Sets the column name transformation to apply during all subsequent file processing.  The closure is passed
     * the raw String value and should return the transformed value.  By default, a no-op transformation is used.
     */
    void setColumnNameTransformer(@ClosureParams(value=SimpleType, options="java.lang.String") Closure<String> columnNameTransformer)

    /**
     * Sets the data cell value transformation to apply during all subsequent file processing.  The closure is passed
     * the raw String value and should return the transformed value.  By default, a no-op transformation is used.
     */
    void setValueTransformer(@ClosureParams(value=SimpleType, options="java.lang.String") Closure<String> valueTransformer)

}