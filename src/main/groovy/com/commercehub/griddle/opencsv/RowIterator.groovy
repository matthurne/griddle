package com.commercehub.griddle.opencsv

import au.com.bytecode.opencsv.CSVReader

class RowIterator implements Iterator<Map<String, String>> {

    private final CSVReader reader
    private final List<String> columnNames
    private final Closure<Boolean> rowSkipCriteria
    private String[] nextRow

    RowIterator(CSVReader reader, List<String> columnNames, Closure<Boolean> rowSkipCriteria) {
        this.reader = reader
        this.columnNames = columnNames
        this.rowSkipCriteria = rowSkipCriteria
        readNextRow()
    }

    @Override
    boolean hasNext() {
        return nextRow != null
    }

    @Override
    Map<String, String> next() {
        if (nextRow == null) {
            throw new NoSuchElementException()
        }
        def nextValue = toExternalRow(nextRow)
        readNextRow()
        return nextValue
    }

    @Override
    void remove() {
        throw new UnsupportedOperationException()
    }

    private void readNextRow() {
        nextRow = reader.readNext()
        while (nextRow != null && rowSkipCriteria(toExternalRow(nextRow))) {
            nextRow = reader.readNext()
        }
        if (nextRow == null) {
            reader.close()
        }
    }

    private Map<String, String> toExternalRow(String[] internalRow) {
        def externalRow = [:]
        columnNames.eachWithIndex{ String columnName, int columnIndex ->
            if (columnName) {
                externalRow[columnName] = internalRow[columnIndex]
            }
        }
        return externalRow
    }

}
