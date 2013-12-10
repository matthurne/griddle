package com.commercehub.griddle.opencsv

import au.com.bytecode.opencsv.CSVReader
import com.commercehub.griddle.TabularData

import static au.com.bytecode.opencsv.CSVParser.*

class CSVTabularData implements TabularData, Closeable {

    private File file
    private final List<String> columnNames
    private final List<CSVReader> readers = []

    CSVTabularData(File file) {
        this.file = file
        def reader = openReader()
        try {
            columnNames = reader.readNext()*.trim()
        } finally {
            closeReader(reader)
        }
    }

    @Override
    Collection<String> getColumnNames() {
        return Collections.unmodifiableCollection(columnNames)
    }

    @Override
    Iterable<Map<String, String>> getRows() {
        return getRows(NEVER_SKIP)
    }

    @Override
    Iterable<Map<String, String>> getRows(Closure<Boolean> rowSkipCriteria) {
        return {
            new RowIterator(openReader(1), columnNames, rowSkipCriteria)
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
        } catch (IOException ignored) {}
        readers.remove(reader)
    }

}
