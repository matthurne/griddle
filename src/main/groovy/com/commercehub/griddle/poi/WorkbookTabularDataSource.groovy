package com.commercehub.griddle.poi

import com.commercehub.griddle.TabularDataSource
import org.apache.poi.ss.usermodel.Workbook

abstract class WorkbookTabularDataSource<T extends Workbook> implements TabularDataSource {

    private final ExcelCellMapper cellMapper

    WorkbookTabularDataSource(ExcelCellMapper cellMapper) {
        this.cellMapper = cellMapper
    }

    @Override
    void withFile(File file, Closure tableHandler) {
        def workbook = openWorkbook(file)
        try {
            def tables = []
            for (sheetIndex in 0..workbook.numberOfSheets-1) {
                tables << new SheetTabularData(workbook.getSheetAt(sheetIndex), cellMapper)
            }
            tableHandler(tables)
        } finally {
            closeWorkbook(workbook)
        }
    }

    protected abstract T openWorkbook(File file)
    protected abstract void closeWorkbook(T workbook)

}
