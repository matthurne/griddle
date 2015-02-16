package com.commercehub.griddle.opencsv

import au.com.bytecode.opencsv.CSVReader
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType

@Deprecated
class RowIterator implements Iterator<Map<String, String>> {

    private final CSVReader reader
    private final Map<Integer,String> transformedColumnNamesByIndex
    private final Closure<String> valueTransformer
    private final Closure<Boolean> rowSkipCriteria
    private String[] nextRow

    RowIterator(CSVReader reader,
                Map<Integer,String> transformedColumnNamesByIndex,
                @ClosureParams(value=SimpleType, options="java.lang.String")
                        Closure<String> valueTransformer,
                @ClosureParams(value=FromString, options="java.util.Map<java.lang.String, java.lang.String>")
                        Closure<Boolean> rowSkipCriteria) {

        this.reader = reader
        this.transformedColumnNamesByIndex = transformedColumnNamesByIndex
        this.valueTransformer = valueTransformer
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
        transformedColumnNamesByIndex.each { Integer columnIndex, String columnName ->
            def columnValue = internalRow[columnIndex]
            if (columnName && columnValue) {
                externalRow[columnName] = valueTransformer(columnValue)
            }
        }
        return externalRow
    }

}
