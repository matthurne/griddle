package com.commercehub.griddle.poi

import com.commercehub.griddle.TabularData
import org.apache.poi.ss.usermodel.Sheet

class SheetTabularData implements TabularData {

    private final Sheet sheet
    private final ExcelCellMapper cellMapper
    private final Map<Integer, String> columns

    SheetTabularData(Sheet sheet, ExcelCellMapper cellMapper) {
        this.sheet = sheet
        this.cellMapper = cellMapper
        columns = sheet.getRow(0).collectEntries  {[it.columnIndex, cellMapper.mapCell(it).trim()]}.findAll {it.value}
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
            new RowIterator(sheet, columns, cellMapper, rowSkipCriteria)
        } as Iterable<Map<String, String>>
    }

}
