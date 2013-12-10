package com.commercehub.griddle.opencsv

import com.commercehub.griddle.BaseTabularDataSource

class CSVTabularDataSource extends BaseTabularDataSource {

    @Override
    void withFile(File file, Closure tableHandler) {
        def table = new CSVTabularData(file, columnNameTransformer, valueTransformer)
        try {
            tableHandler([table])
        } finally {
            table.close()
        }
    }

}
