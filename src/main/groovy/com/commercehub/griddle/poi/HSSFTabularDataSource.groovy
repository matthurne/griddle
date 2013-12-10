package com.commercehub.griddle.poi

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem

class HSSFTabularDataSource extends WorkbookTabularDataSource<HSSFWorkbook> {

    /**
     * Creates a new instance that uses a {@link SimpleExcelCellMapper}.  In most cases, you'll instead want to provide
     * your own cell mapper.
     */
    HSSFTabularDataSource() {
        this(new SimpleExcelCellMapper())
    }

    HSSFTabularDataSource(ExcelCellMapper cellMapper) {
        super(cellMapper)
    }

    @Override
    protected HSSFWorkbook openWorkbook(File file) {
        def fs = new NPOIFSFileSystem(file)
        return new HSSFWorkbook(fs.root, true)
    }

    @Override
    protected void closeWorkbook(HSSFWorkbook workbook) {
        workbook.rootDirectory.NFileSystem.close()
    }

}
