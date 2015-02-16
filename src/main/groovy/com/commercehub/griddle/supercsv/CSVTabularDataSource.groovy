package com.commercehub.griddle.supercsv

import com.commercehub.griddle.BaseTabularDataSource
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

class CSVTabularDataSource extends BaseTabularDataSource {

    @Override
    void withFile(File file,
                  @ClosureParams(value=FromString, options="java.lang.Iterable<com.commercehub.griddle.TabularData>")
                          Closure tableHandler) {

        def table = new CSVTabularData(file, columnNameTransformer, valueTransformer)
        try {
            tableHandler([table])
        } finally {
            table.close()
        }
    }

}
