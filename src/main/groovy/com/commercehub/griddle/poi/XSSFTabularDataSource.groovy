package com.commercehub.griddle.poi

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class XSSFTabularDataSource extends WorkbookTabularDataSource<XSSFWorkbook> {

    /**
     * Creates a new instance that uses a {@link SimpleExcelCellMapper}.  In most cases, you'll instead want to provide
     * your own cell mapper.
     */
    XSSFTabularDataSource() {
        this(new SimpleExcelCellMapper())
    }

    XSSFTabularDataSource(ExcelCellMapper cellMapper) {
        super(cellMapper)
    }

    @Override
    protected XSSFWorkbook openWorkbook(File file) {
        def pkg = OPCPackage.open(file, PackageAccess.READ)
        return new XSSFWorkbook(pkg)
    }

    @Override
    protected void closeWorkbook(XSSFWorkbook workbook) {
        workbook.package.revert()
    }

}
