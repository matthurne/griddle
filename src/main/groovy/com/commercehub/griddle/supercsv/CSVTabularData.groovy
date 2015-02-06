package com.commercehub.griddle.supercsv

import com.commercehub.griddle.TabularData
import org.supercsv.io.CsvMapReader
import org.supercsv.io.ICsvMapReader
import org.supercsv.prefs.CsvPreference

class CSVTabularData implements TabularData, Closeable {

    private File file
    private final Closure<String> valueTransformer
    private final Map<Integer,String> transformedColumnNamesByIndex
    private final List<String> transformedColumnNames
    private final List<ICsvMapReader> readers = []

    CSVTabularData(File file, Closure<String> columnNameTransformer, Closure<String> valueTransformer) {
        this.file = file
        this.valueTransformer = valueTransformer

        transformedColumnNamesByIndex = [:]
        transformedColumnNames = []

        def reader = openReader()
        try {
            def headerRow = Arrays.asList(reader.getHeader(true))
            headerRow?.eachWithIndex { String columnName, int index ->
                def transformedColumnName = columnNameTransformer(columnName)
                if (transformedColumnName) {
                    transformedColumnNamesByIndex[index] = transformedColumnName
                    transformedColumnNames << transformedColumnName
                }
            }
        } finally {
            closeReader(reader)
        }
    }

    @Override
    List<String> getColumnNames() {
        return Collections.unmodifiableList(transformedColumnNames)
    }

    @Override
    Iterable<Map<String, String>> getRows() {
        return getRows(NEVER_SKIP)
    }

    @Override
    Iterable<Map<String, String>> getRows(Closure<Boolean> rowSkipCriteria) {
        return {
            new RowIterator(openReader(), transformedColumnNamesByIndex, valueTransformer, rowSkipCriteria)
        } as Iterable<Map<String, String>>
    }

    @Override
    void close() {
        file = null
        for (reader in new ArrayList<ICsvMapReader>(readers)) {
            closeReader(reader)
        }
    }

    private ICsvMapReader openReader() {
        if (file == null) {
            throw new IllegalStateException("No file available")
        }

        def reader = new CsvMapReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE)
        readers << reader
        return reader
    }

    private void closeReader(ICsvMapReader reader) {
        try {
            reader.close()
        } catch (IOException ignored) { }
        readers.remove(reader)
    }

}
