package com.commercehub.griddle.opencsv

import com.commercehub.griddle.TabularDataSource

class CSVTabularDataSource implements TabularDataSource {

    @Override
    void withFile(File file, Closure tableHandler) {
        def table = new CSVTabularData(file)
        try {
            tableHandler([table])
        } finally {
            table.close()
        }
    }

}
