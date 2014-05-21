package com.commercehub.griddle.poi.streaming

import com.commercehub.griddle.TabularData

import org.apache.poi.hssf.util.CellReference
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.model.StylesTable
import org.xml.sax.InputSource
import org.xml.sax.SAXException

import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

class StreamingXSSFTabularData implements TabularData {

    protected final Closure<String> valueTransformer

    protected final InputStream inputStream
    protected final Map<Integer, String> transformedColumnNamesByIndex
    protected final List<String> transformedColumnNames

    protected SheetDataContainer dataContainer
    protected use1904DateWindowing

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

		transformedColumnNamesByIndex = [:]
		transformedColumnNames = []
		
		dataContainer.headers?.each {
			def transformedColumnName = columnNameTransformer(it.value)
			if (transformedColumnName) {
				transformedColumnNamesByIndex << [(it.key):transformedColumnName]
				transformedColumnNames << transformedColumnName
			}
		}
    }

    @Override
    List<String> getColumnNames() {
        return Collections.unmodifiableList(transformedColumnNames)
    }

    @Override
    Iterable<Map<String, String>> getRows() {
        return getRows(NEVER_SKIP)
    }

    // column name -> value
    @Override
    Iterable<Map<String, String>> getRows(Closure<Boolean> rowSkipCriteria) {
        return {
            new SheetDataContainerBackedRowIterator(dataContainer, transformedColumnNamesByIndex, valueTransformer, rowSkipCriteria)
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
        def saxFactory = SAXParserFactory.newInstance()
        SAXParser saxParser = saxFactory.newSAXParser()
        def sheetParser = saxParser.XMLReader
        this.use1904DateWindowing = use1904DateWindowing
        org.xml.sax.ContentHandler handler = handlerBuilder(stylesTable, sharedStringsTable, sheetContentsExtractor)

        sheetParser.setContentHandler(handler)
        sheetParser.parse(sheetSource)
    }

    // XSSFSheetXMLHandler does not seem to abide to 1904DateWindowing.
    // It also formats per excel formatting rules.  If you want more raw data out, override this and
    // return your own XSSFSheetXMLHandler handler.
    protected XSSFSheetXMLHandler handlerBuilder(StylesTable stylesTable, ReadOnlySharedStringsTable sst,
                                                 XSSFSheetXMLHandler.SheetContentsHandler sheetContentsExtractor) {

        def formatter = new DataFormatter()
        return new XSSFSheetXMLHandler(
                stylesTable, sst, sheetContentsExtractor, formatter, false)
    }

    protected class SheetDataExtractor implements XSSFSheetXMLHandler.SheetContentsHandler {
        boolean atHeaderRow = true
        SheetDataContainer dataContainer
        int currentRowNumber
        Map<Integer, String> currentRow

        protected SheetDataExtractor(SheetDataContainer dataContainer) {
            this.dataContainer = dataContainer
        }

        void startRow(int rowNum) {
            currentRowNumber = rowNum
            if (currentRowNumber == 0) {
                atHeaderRow = true
            }

            currentRow = new TreeMap<Integer, String>()
        }

        void endRow() {
            if (atHeaderRow) {
                atHeaderRow = false
                dataContainer.setHeaders(currentRow)
            } else {
                dataContainer.addReplaceRow(currentRowNumber, currentRow)
            }
        }

        void cell(String cellRef, String formattedValue) {
            def ref = new CellReference(cellRef)
            currentRow.put((Integer) ref.col, formattedValue)
        }

        @SuppressWarnings(["EmptyMethod", "UnusedMethodParameter"])
        void headerFooter(String text, boolean isHeader, String tagName) {
            // ignoring, we have no need to identify an excel header or footer.
        }
    }
}
