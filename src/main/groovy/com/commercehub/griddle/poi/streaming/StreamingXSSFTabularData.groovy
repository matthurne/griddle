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

class StreamingXSSFTabularData implements TabularData {

    protected final Closure<String> valueTransformer

    protected final InputStream inputStream
    protected final Map<Integer, String> transformedColumnNames

    protected SheetDataContainer dataContainer

    StreamingXSSFTabularData(InputStream inputStream,
                             StylesTable stylesTable,
                             ReadOnlySharedStringsTable sharedStringsTable,
                             Closure<String> columnNameTransformer,
                             Closure<String> valueTransformer,
                             boolean use1904DateWindowing) {

        this.inputStream = inputStream
        this.valueTransformer = valueTransformer
        this.dataContainer = new SheetDataContainer()

        loadSheet(new SheetDataExtractor(dataContainer), stylesTable, sharedStringsTable, inputStream, use1904DateWindowing)

        if (dataContainer.getHeaders()) {
            transformedColumnNames = dataContainer.getHeaders().collectEntries {
                [(Integer) it.key, columnNameTransformer(it.value)]
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
            new SheetDataContainerBackedRowIterator(dataContainer, transformedColumnNames, valueTransformer, rowSkipCriteria)
        } as Iterable<Map<String, String>>
    }

    /**
     * Processes the given sheet
     */
    protected void loadSheet(
            XSSFSheetXMLHandler.SheetContentsHandler sheetContentsExtractor,
            StylesTable stylesTable,
            ReadOnlySharedStringsTable sharedStringsTable,
            InputStream sheetInputStream, boolean use1904DateWindowing)
            throws IOException, SAXException {

        def sheetSource = new InputSource(sheetInputStream)
        SAXParserFactory saxFactory = SAXParserFactory.newInstance()
        try {
            SAXParser saxParser = saxFactory.newSAXParser()
            XMLReader sheetParser = saxParser.getXMLReader()
            org.xml.sax.ContentHandler handler = buildHandler(stylesTable, sharedStringsTable, sheetContentsExtractor,
                    use1904DateWindowing);

            sheetParser.setContentHandler(handler)
            sheetParser.parse(sheetSource)
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage())
        }
    }

    // XSSFSheetXMLHandler does not seem to abide to 1904DateWindowing.
    // It also formats per excel formatting rules.  If you want more raw data out, override this and
    // return your own XSSFSheetXMLHandler handler.
    protected XSSFSheetXMLHandler buildHandler(StylesTable stylesTable, ReadOnlySharedStringsTable sst,
                                               XSSFSheetXMLHandler.SheetContentsHandler sheetContentsExtractor,
                                               boolean use1904DateWindowing) {
        def formatter = new DataFormatter()
        return new XSSFSheetXMLHandler(
                stylesTable, sst, sheetContentsExtractor, formatter, false)
    }

    protected class SheetDataExtractor implements XSSFSheetXMLHandler.SheetContentsHandler {
        def boolean atHeaderRow = true
        def dataContainer
        def currentRowNumber
        def Map<Integer, String> currentRow

        protected SheetDataExtractor(SheetDataContainer dataContainer) {
            this.dataContainer = dataContainer
        }

        public void startRow(int rowNum) {
            currentRowNumber = rowNum
            if (currentRowNumber == 0) {
                atHeaderRow = true
            }

            currentRow = new TreeMap<Integer, String>()
        }

        public void endRow() {
            if (atHeaderRow) {
                atHeaderRow = false
                dataContainer.setHeaders(currentRow)
            } else {
                dataContainer.addReplaceRow(currentRowNumber, currentRow)
            }
        }

        public void cell(String cellRef, String formattedValue) {
            def ref = new CellReference(cellRef)
            currentRow.put((Integer) ref.getCol(), formattedValue)
        }

        public void headerFooter(String text, boolean isHeader, String tagName) {
            // Not what you would think
        }
    }
}
