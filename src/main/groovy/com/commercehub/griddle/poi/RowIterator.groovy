package com.commercehub.griddle.poi

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

class RowIterator implements Iterator<Map<String, String>> {

    private final Map<Integer, String> columns
    private final ExcelCellMapper cellMapper
    private final Closure<String> valueTransformer
    private final Closure<Boolean> rowSkipCriteria
    private final Iterator<Row> rowIterator
    private Map<String, String> nextValue

    RowIterator(Sheet sheet, Map<Integer, String> columns, Closure<String> valueTransformer, ExcelCellMapper cellMapper,
                Closure<Boolean> rowSkipCriteria) {
        this.columns = columns
        this.valueTransformer = valueTransformer
        this.cellMapper = cellMapper
        this.rowSkipCriteria = rowSkipCriteria
        this.rowIterator = sheet.rowIterator()
        if (columns) {
            if (rowIterator.hasNext()) {
                rowIterator.next() // Skip header row
            }
            readNextRow()
        } else {
            nextValue = null // If there aren't any defined columns, there will never be any meaningful row data
        }
    }

    @Override
    boolean hasNext() {
        return nextValue != null
    }

    @Override
    Map<String, String> next() {
        if (nextValue == null) {
            throw new NoSuchElementException()
        }
        Map<String, String> valueToReturn = nextValue
        readNextRow()
        return valueToReturn
    }

    @Override
    void remove() {
        throw new UnsupportedOperationException()
    }

    private void readNextRow() {
        nextValue = null
        while (rowIterator.hasNext() && nextValue == null) {
            def row = rowIterator.next()
            Map<String, String> nextValueCandidate = toExternalRow(row)
            if (!rowSkipCriteria(nextValueCandidate)) {
                nextValue = nextValueCandidate
            }
        }
    }

    private Map<String, String> toExternalRow(Row internalRow) {
        def externalRow = [:]
        for (cell in internalRow.cellIterator()) {
            def columnName = columns[cell.columnIndex]
            if (columnName) {
                externalRow[columnName] = valueTransformer(cellMapper.mapCell(cell))
            }
        }
        return externalRow
    }

}
