package com.commercehub.griddle.poi

import com.commercehub.griddle.Transformers
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import spock.lang.Specification

class SheetTabularDataSpec extends Specification {

    def cellMapper = Mock(ExcelCellMapper)

    def setup() {
        cellMapper.mapCell(_) >> { Cell cell -> cell.stringCellValue }
    }

    def "column name transformers are applied"() {
        setup:
        def valueTransformer = Transformers.noop
        def headerRow = mockRow([
                "First Name", "", "Last Name", "  ", "  Lead Space", "Trail tab\t", " Outerspace "
        ], "headerRow")
        def sheet = mockSheet(headerRow)

        when:
        def tabularData = new SheetTabularData(sheet, columnNameTransformer, valueTransformer, cellMapper)

        then:
        tabularData.columnNames.collect() == expectedColumnNames

        where:
        columnNameTransformer      | expectedColumnNames
        Transformers.noop          | ["First Name", "Last Name", "  ", "  Lead Space", "Trail tab\t", " Outerspace "]
        Transformers.lowercase     | ["first name", "last name", "  ", "  lead space", "trail tab\t", " outerspace "]
        Transformers.trim          | ["First Name", "Last Name", "Lead Space", "Trail tab", "Outerspace"]
        Transformers.trimLowercase | ["first name", "last name", "lead space", "trail tab", "outerspace"]
    }

    def "value transformers are applied"() {
        setup:
        def columnNameTransformer = Transformers.noop
        def headerRow = mockRow([
                "ID", "FName", "LName", "Email", "Dept"
        ], "headerRow")
        def dataRow = mockRow([
                "12345", "Joe ", " Wu", " joe@wu.name ", " "
        ], "dataRow")
        def sheet = mockSheet(headerRow, dataRow)

        when:
        def tabularData = new SheetTabularData(sheet, columnNameTransformer, valueTransformer, cellMapper)

        then:
        tabularData.rows.collect() == expectedRows

        where:
        valueTransformer           | expectedRows
        Transformers.noop          | [[ID: "12345", FName: "Joe ", LName: " Wu", Email: " joe@wu.name ", Dept: " "]]
        Transformers.lowercase     | [[ID: "12345", FName: "joe ", LName: " wu", Email: " joe@wu.name ", Dept: " "]]
        Transformers.trim          | [[ID: "12345", FName: "Joe", LName: "Wu", Email: "joe@wu.name", Dept: ""]]
        Transformers.trimLowercase | [[ID: "12345", FName: "joe", LName: "wu", Email: "joe@wu.name", Dept: ""]]
    }

    Sheet mockSheet(Row headerRow, Row... dataRows) {
        def sheet = Mock(Sheet)
        def rows = []
        rows << headerRow
        if (dataRows) {
            rows.addAll(dataRows)
        }
        sheet.getRow(0) >> headerRow
        sheet.iterator() >> { rows.iterator() }
        sheet.rowIterator() >> { rows.iterator() }
        return sheet
    }

    Row mockRow(List<String> cellValues, String name) {
        def row = Mock(Row, name: name)
        def cells = []
        cellValues.eachWithIndex { String cellValue, int columnIndex ->
            cells << mockCell(columnIndex, cellValue)
        }
        row.iterator() >> { cells.iterator() }
        row.cellIterator() >> { cells.iterator() }
        return row
    }

    Cell mockCell(int columnIndex, String value) {
        def cell = Mock(Cell)
        cell.columnIndex >> columnIndex
        cell.stringCellValue >> value
        return cell
    }

}
