package com.commercehub.griddle.opencsv

import au.com.bytecode.opencsv.CSVReader
import com.commercehub.griddle.TabularData

import static au.com.bytecode.opencsv.CSVParser.*

@Deprecated
class CSVTabularData implements TabularData, Closeable {

    private File file
    private final Closure<String> valueTransformer
    private final Map<Integer,String> transformedColumnNamesByIndex
    private final List<String> transformedColumnNames
    private final List<CSVReader> readers = []

    CSVTabularData(File file, Closure<String> columnNameTransformer, Closure<String> valueTransformer) {
        this.file = file
        this.valueTransformer = valueTransformer

        transformedColumnNamesByIndex = [:]
        transformedColumnNames = []

        def reader = openReader()
        try {
            def headerRow = reader.readNext()

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
            new RowIterator(openReader(1), transformedColumnNamesByIndex, valueTransformer, rowSkipCriteria)
        } as Iterable<Map<String, String>>
    }

    @Override
    void close() throws IOException {
        file = null
        for (reader in new ArrayList<CSVReader>(readers)) {
            closeReader(reader)
        }
    }

    private CSVReader openReader(int startingLine = 0) {
        if (file == null) {
            throw new IllegalStateException("No file available")
        }
        def reader = new CSVReader(new FileReader(file), DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHARACTER,
                DEFAULT_ESCAPE_CHARACTER, startingLine)
        readers.add(reader)
        return reader
    }

    private void closeReader(CSVReader reader) {
        try {
            reader.close()
        } catch (IOException ignored) { }
        readers.remove(reader)
    }

}
