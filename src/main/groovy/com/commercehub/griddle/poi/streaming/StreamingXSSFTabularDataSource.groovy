package com.commercehub.griddle.poi.streaming

import com.commercehub.griddle.BaseTabularDataSource
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.model.StylesTable
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument

class StreamingXSSFTabularDataSource extends BaseTabularDataSource {

    protected OPCPackage ocpPackage

    public StreamingXSSFTabularDataSource() {}

    @Override
    void withFile(File file, Closure tableHandler) {
        try {
            def tables = []

            def reader = getReader(file);
            def sst = new ReadOnlySharedStringsTable(ocpPackage)
            def stylesTable = reader.stylesTable

            def use1904DateWindowing = getWorkbookUses1904DateWindowing(reader)

            for (sheetInputStream in reader.sheetsData) {
                tables << buildStreamingXSSFTabularData(sheetInputStream, stylesTable, sst, use1904DateWindowing);
                sheetInputStream.close()
            }
            tableHandler(tables)
        } finally {
            cleanup()
        }
    }

    protected StreamingXSSFTabularData buildStreamingXSSFTabularData(InputStream sheetInputStream, StylesTable stylesTable,
                                                            ReadOnlySharedStringsTable sst,
                                                            boolean use1904DateWindowing) {
        return new StreamingXSSFTabularData(sheetInputStream, stylesTable, sst, columnNameTransformer,
                valueTransformer, use1904DateWindowing)
    }

    protected boolean getWorkbookUses1904DateWindowing(XSSFReader reader) {
        def workbookXML = reader.getWorkbookData();
        def workbookDocument = WorkbookDocument.Factory.parse(workbookXML)
        def workbook = workbookDocument.getWorkbook()
        def prefix = workbook.getWorkbookPr()
        return prefix.getDate1904()
    }

    protected XSSFReader getReader(File file) {
        ocpPackage = OPCPackage.open(file, PackageAccess.READ)
        return new XSSFReader(ocpPackage)
    }

    protected void cleanup() {
        if (ocpPackage != null) {
            ocpPackage.close()
        }
    }
}
