package com.commercehub.griddle.supercsv

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType
import org.supercsv.io.ICsvListReader

class RowIterator implements Iterator<Map<String, String>> {

    private final ICsvListReader reader
    private final Map<Integer,String> transformedColumnNamesByIndex
    private final Closure<String> valueTransformer
    private final Closure<Boolean> rowSkipCriteria
    private final String[] header
    private List<String> nextRow

    RowIterator(ICsvListReader reader,
                Map<Integer,String> transformedColumnNamesByIndex,
                @ClosureParams(value=SimpleType, options="java.lang.String")
                        Closure<String> valueTransformer,
                @ClosureParams(value=FromString, options="java.util.Map<java.lang.String, java.lang.String>")
                        Closure<Boolean> rowSkipCriteria) {

        this.reader = reader
        this.transformedColumnNamesByIndex = transformedColumnNamesByIndex
        this.valueTransformer = valueTransformer
        this.rowSkipCriteria = rowSkipCriteria
        header = reader.getHeader(true)
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
        nextRow = reader.read()
        while (nextRow != null && rowSkipCriteria(toExternalRow(nextRow))) {
            nextRow = reader.read()
        }
        if (nextRow == null) {
            reader.close()
        }
    }

    private Map<String, String> toExternalRow(List<String> internalRow) {
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
