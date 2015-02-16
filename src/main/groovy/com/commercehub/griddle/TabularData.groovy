package com.commercehub.griddle

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

interface TabularData {

    final Closure<Boolean> NEVER_SKIP = { false }

    List<String> getColumnNames()

    Iterable<Map<String, String>> getRows()

    /**
     * Provides access to the rows for this objects.  The provided rowSkipCriteria is used to determine which rows
     * should be skipped.  The closure is passed the row as the only argument.  If the closure returns true, the
     * row will be skipped.  If you don't want to skip any rows, use {@link #NEVER_SKIP} (or the convenience signature
     * that has no arguments).
     */
    Iterable<Map<String, String>> getRows(@ClosureParams(value=FromString, options="java.util.Map<java.lang.String, java.lang.String>")
                                                  Closure<Boolean> rowSkipCriteria)

}
