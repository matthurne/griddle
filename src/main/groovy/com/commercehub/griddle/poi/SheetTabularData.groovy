package com.commercehub.griddle.poi

import com.commercehub.griddle.TabularData
import org.apache.poi.ss.usermodel.Sheet

class SheetTabularData implements TabularData {

    private final Sheet sheet
    private final Closure<String> valueTransformer
    private final ExcelCellMapper cellMapper
    private final Map<Integer, String> columns

    SheetTabularData(Sheet sheet, Closure<String> columnNameTransformer, Closure<String> valueTransformer,
                     ExcelCellMapper cellMapper) {
        this.sheet = sheet
        this.valueTransformer = valueTransformer
        this.cellMapper = cellMapper
        def headerRow = sheet.getRow(0)
        if (headerRow) {
            columns = headerRow.collectEntries {
                [it.columnIndex, columnNameTransformer(cellMapper.mapCell(it))]
            }.findAll { it.value }
        } else {
            columns = [:]
        }
    }

    @Override
    Collection<String> getColumnNames() {
        return Collections.unmodifiableCollection(columns.values())
    }

    @Override
    Iterable<Map<String, String>> getRows() {
        return getRows(NEVER_SKIP)
    }

    @Override
    Iterable<Map<String, String>> getRows(Closure<Boolean> rowSkipCriteria) {
        return {
            new RowIterator(sheet, columns, valueTransformer, cellMapper, rowSkipCriteria)
        } as Iterable<Map<String, String>>
    }

}
