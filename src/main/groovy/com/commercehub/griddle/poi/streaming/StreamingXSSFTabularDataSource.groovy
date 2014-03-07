package com.commercehub.griddle.poi.streaming

import com.commercehub.griddle.BaseTabularDataSource
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.model.StylesTable

class StreamingXSSFTabularDataSource extends BaseTabularDataSource {
    private final ExcelCellElementMapper cellMapper
    private OPCPackage ocpPackage


    public StreamingXSSFTabularDataSource() {
        this.cellMapper = new SimpleExcelCellElementMapper();
    }

    public StreamingXSSFTabularDataSource(ExcelCellElementMapper cellMapper) {
        this.cellMapper = cellMapper
    }

    @Override
    void withFile(File file, Closure tableHandler) {
        try {
            List<StreamingXSSFTabularData> tables = []

            XSSFReader reader = getReader(file);
            ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(ocpPackage);
            StylesTable stylesTable = reader.getStylesTable();

            for (InputStream sheetInputStream in reader.getSheetsData()) {
                tables << new StreamingXSSFTabularData(sheetInputStream, stylesTable, sst, columnNameTransformer, valueTransformer, cellMapper)
                sheetInputStream.close()
            }
            tableHandler(tables)
        } finally {
            cleanup();
        }
    }

    private getReader(File file) {
        ocpPackage = OPCPackage.open(file, PackageAccess.READ);
        return new XSSFReader(ocpPackage);
    }

    private cleanup() {
        if (ocpPackage != null) {
            ocpPackage.close();
        }
    }
}
