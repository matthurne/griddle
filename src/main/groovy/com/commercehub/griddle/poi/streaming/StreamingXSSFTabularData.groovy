package com.commercehub.griddle.poi.streaming

import com.commercehub.griddle.TabularData
import org.apache.poi.hssf.util.CellReference
import org.apache.poi.ss.usermodel.DataFormatter

import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.model.StylesTable
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader

import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

class StreamingXSSFTabularData implements TabularData, Closeable {

    private final Closure<String> valueTransformer

    private final InputStream inputStream
    private final ExcelCellElementMapper cellValueMapper
    private final Map<Integer, String> transformedColumnNames

    private SheetDataContainer dataContainer;

    StreamingXSSFTabularData(InputStream inputStream,
                             StylesTable stylesTable,
                             ReadOnlySharedStringsTable sharedStringsTable,
                             Closure<String> columnNameTransformer,
                             Closure<String> valueTransformer,
                             ExcelCellElementMapper cellValueMapper) {

        this.inputStream = inputStream
        this.valueTransformer = valueTransformer
        this.cellValueMapper = new SimpleExcelCellElementMapper(); //cellValueMapper
        this.dataContainer = new SheetDataContainer();

        loadSheet(new SheetDataExtractor(dataContainer), stylesTable, sharedStringsTable, inputStream);

        if (dataContainer.getHeaders()) {
            transformedColumnNames = dataContainer.getHeaders().collectEntries {
                [(Integer) it.key, columnNameTransformer(cellValueMapper.mapStringValue(it.value))]
            }.findAll { it.value }
        } else {
            transformedColumnNames = [:]
        }
    }

    @Override
    Collection<String> getColumnNames() {
        Collections.unmodifiableCollection(transformedColumnNames.values())
    }

    @Override
    Iterable<Map<String, String>> getRows() {
        return getRows(NEVER_SKIP)
    }

    // column name -> value
    @Override
    Iterable<Map<String, String>> getRows(Closure<Boolean> rowSkipCriteria) {
        return {
            new SheetDataContainerBackedRowIterator(dataContainer, transformedColumnNames, valueTransformer, cellValueMapper, rowSkipCriteria)
        } as Iterable<Map<String, String>>
    }

    @Override
    void close() throws IOException {

    }

    /**
     * Processes the given sheet
     */
    protected void loadSheet(
            XSSFSheetXMLHandler.SheetContentsHandler sheetContentsExtractor,
            StylesTable stylesTable,
            ReadOnlySharedStringsTable sharedStringsTable,
            InputStream sheetInputStream)
            throws IOException, SAXException {

        DataFormatter formatter = new DataFormatter();

        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxFactory.newSAXParser();
            XMLReader sheetParser = saxParser.getXMLReader();
            org.xml.sax.ContentHandler handler = new XSSFSheetXMLHandler(
                    stylesTable, sharedStringsTable, sheetContentsExtractor, formatter, cellValueMapper.shouldCaclulateFormulas());
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
        }
    }

    protected class SheetDataExtractor implements XSSFSheetXMLHandler.SheetContentsHandler {
        private boolean atHeaderRow = true;
        SheetDataContainer dataContainer;
        Integer currentRowNumber;
        Map<Integer, String> currentRow;

        protected SheetDataExtractor(SheetDataContainer dataContainer) {
            this.dataContainer = dataContainer;
        }

        public void startRow(int rowNum) {
            currentRowNumber = rowNum;
            if (currentRowNumber == 0) {
                atHeaderRow = true;
            }

            currentRow = new TreeMap<Integer, String>();
        }

        public void endRow() {
            if (atHeaderRow) {
                atHeaderRow = false;
                dataContainer.setHeaders(currentRow);
            } else {
                dataContainer.addReplaceRow(currentRowNumber, currentRow);
            }
        }

        public void cell(String cellRef, String formattedValue) {
            CellReference ref = new CellReference(cellRef);
            currentRow.put((Integer) ref.getCol(), formattedValue);
        }

        public void headerFooter(String text, boolean isHeader, String tagName) {
            // Not what you would think
        }
    }
}
