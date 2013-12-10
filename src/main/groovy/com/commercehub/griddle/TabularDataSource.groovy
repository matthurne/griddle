package com.commercehub.griddle

interface TabularDataSource {

    /**
     * Opens a file and allows processing it within a closure.  The closure is passed an Iterable<TabularData>.
     */
    void withFile(File file, Closure tableHandler)

}