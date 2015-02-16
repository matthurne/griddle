package com.commercehub.griddle.supercsv

import com.commercehub.griddle.TabularData
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType
import org.supercsv.io.CsvListReader
import org.supercsv.io.ICsvListReader
import org.supercsv.prefs.CsvPreference

class CSVTabularData implements TabularData, Closeable {

    private File file
    private final Closure<String> valueTransformer
    private final Map<Integer,String> transformedColumnNamesByIndex
    private final List<String> transformedColumnNames
    private final List<ICsvListReader> readers = []

    CSVTabularData(File file,
                   @ClosureParams(value=SimpleType, options="java.lang.String") Closure<String> columnNameTransformer,
                   @ClosureParams(value=SimpleType, options="java.lang.String") Closure<String> valueTransformer) {

        this.file = file
        this.valueTransformer = valueTransformer

        transformedColumnNamesByIndex = [:]
        transformedColumnNames = []

        def reader = openReader()
        try {
            def headerRow = Arrays.asList(reader.getHeader(true))
            headerRow?.eachWithIndex { String columnName, int index ->
                if (columnName != null) {
                    def transformedColumnName = columnNameTransformer(columnName)
                    if (transformedColumnName) {
                        transformedColumnNamesByIndex[index] = transformedColumnName
                        transformedColumnNames << transformedColumnName
                    }
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
    Iterable<Map<String, String>> getRows(@ClosureParams(value=FromString, options="java.util.Map<java.lang.String, java.lang.String>")
                                                  Closure<Boolean> rowSkipCriteria) {

        return {
            new RowIterator(openReader(), transformedColumnNamesByIndex, valueTransformer, rowSkipCriteria)
        } as Iterable<Map<String, String>>
    }

    @Override
    void close() {
        file = null
        for (reader in new ArrayList<ICsvListReader>(readers)) {
            closeReader(reader)
        }
    }

    private ICsvListReader openReader() {
        if (file == null) {
            throw new IllegalStateException("No file available")
        }

        def reader = new CsvListReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE)
        readers << reader
        return reader
    }

    private void closeReader(ICsvListReader reader) {
        try {
            reader.close()
        } catch (IOException ignored) { }
        readers.remove(reader)
    }

}
