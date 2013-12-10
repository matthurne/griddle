package com.commercehub.griddle.poi

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

class RowIterator implements Iterator<Map<String, String>> {

    private final Map<Integer, String> columns
    private final ExcelCellMapper cellMapper
    private final Closure<Boolean> rowSkipCriteria
    private final Iterator<Row> rowIterator
    private Row nextRow

    RowIterator(Sheet sheet, Map<Integer, String> columns, ExcelCellMapper cellMapper,
                Closure<Boolean> rowSkipCriteria) {
        this.columns = columns
        this.cellMapper = cellMapper
        this.rowSkipCriteria = rowSkipCriteria
        this.rowIterator = sheet.rowIterator()
        if (columns) {
            if (rowIterator.hasNext()) {
                rowIterator.next() // Skip header row
            }
            readNextRow()
        } else {
            nextRow = null // If there aren't any defined columns, there will never be any meaningful row data
        }
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
        nextRow = null
        while (rowIterator.hasNext() && nextRow == null) {
            def row = rowIterator.next()
            if (!rowSkipCriteria(toExternalRow(row))) {
                nextRow = row
            }
        }
    }

    private Map<String, String> toExternalRow(Row internalRow) {
        def externalRow = [:]
        for (cell in internalRow.cellIterator()) {
            def columnName = columns[cell.columnIndex]
            if (columnName) {
                externalRow[columnName] = cellMapper.mapCell(cell)
            }
        }
        return externalRow
    }

}
