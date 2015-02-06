package com.commercehub.griddle.opencsv

import com.commercehub.griddle.BaseTabularDataSource

/**
 * @deprecated This class will be removed in a future release. Use
 * {@link com.commercehub.griddle.supercsv.CSVTabularDataSource} instead, which is a drop-in replacement.
 */
@Deprecated
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
