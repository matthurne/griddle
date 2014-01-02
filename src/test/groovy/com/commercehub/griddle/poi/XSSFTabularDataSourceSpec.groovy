package com.commercehub.griddle.poi

import com.commercehub.griddle.TabularData
import spock.lang.Specification

class XSSFTabularDataSourceSpec extends Specification {

    private static final File FILE1 = getFile("/xssf/spreadsheet1.xlsx")
    private static final File FILE2 = getFile("/xssf/spreadsheet2.xlsx")
    private static final File FILE3 = getFile("/xssf/spreadsheet3.xlsx")
    private static final File FILE4 = getFile("/xssf/spreadsheet4.xlsx")
    private static final File FILE5 = getFile("/xssf/spreadsheet5.xlsx") // sheet with no rows

    def tabularDataSource = new XSSFTabularDataSource()

    def "withFile produces one table per worksheet"() {
        int actualTableCount = -1

        when:
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            actualTableCount = tables.collect().size()
        }

        then:
        actualTableCount == expectedTableCount

        where:
        file  | expectedTableCount
        FILE1 | 4
        FILE2 | 1
        FILE3 | 1
        FILE4 | 1
        FILE5 | 2
    }

    def "tables have expected columnNames"() {
        def actualColumnNames = []

        when:
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            for (table in tables) {
                actualColumnNames << table.columnNames.collect()
            }
        }

        then:
        actualColumnNames == expectedColumnNames

        where:
        file  | expectedColumnNames
        FILE1 | [["Foo", "Bar", "Moo", "Goo"], ["Zip-a-Dee-Doo-Dah", "Zip-a-dee-ay"], [], ["Column 1", "Column 2", "Column 4"]]
        FILE2 | [["Zip-a-Dee-Doo-Dah", "Zip-a-dee-ay"]]
        FILE3 | [[]]
        FILE4 | [["Column 1", "Column 2", "Column 4"]]
        FILE5 | [["Column A", "Column B"], []]
    }

    @SuppressWarnings("LineLength")
    def "table rows have expected values"() {
        def actualRows = []

        when:
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            for (table in tables) {
                actualRows << table.rows.collect()
            }
        }

        then:
        actualRows == expectedRows

        where:
        file  | expectedRows
        FILE1 | [[["Foo":"Lorem", "Bar":"ipsum", "Moo":"dolor", "Goo":"sit"], ["Foo":"", "Bar":"amet", "Moo":"consectetur", "Goo":""], ["Foo":"adipisicing", "Bar":"", "Moo":"", "Goo":"elit, sed do"]], [["Zip-a-Dee-Doo-Dah":"My, oh my what a wonderful day!", "Zip-a-dee-ay":"Plenty of sunshine heading my way"], ["Zip-a-Dee-Doo-Dah":"Zip-a-Dee-Doo-Dah", "Zip-a-dee-ay":"Zip-a-dee-ay"]], [], [["Column 1":"Row 1 Column 1", "Column 4":"Row 1 Column 4"], ["Column 1":"Row 3 Column 1", "Column 2":"Row 3 Column 2", "Column 4":"Row 3 Column 4"], ["Column 1":"Row 4 Column 1", "Column 2":"Row 4 Column 2", "Column 4":"Row 4 Column 4"]]]
        FILE2 | [[["Zip-a-Dee-Doo-Dah":"My, oh my what a wonderful day!", "Zip-a-dee-ay":"Plenty of sunshine heading my way"], ["Zip-a-Dee-Doo-Dah":"Zip-a-Dee-Doo-Dah", "Zip-a-dee-ay":"Zip-a-dee-ay"]]]
        FILE3 | [[]]
        FILE4 | [[["Column 1":"Row 1 Column 1", "Column 4":"Row 1 Column 4"], ["Column 1":"Row 3 Column 1", "Column 2":"Row 3 Column 2", "Column 4":"Row 3 Column 4"], ["Column 1":"Row 4 Column 1", "Column 2":"Row 4 Column 2", "Column 4":"Row 4 Column 4"]]]
        FILE5 | [[["Column A":"A2", "Column B":"B2"], ["Column A":"A3", "Column B":"B3"]], []]
    }

    private static File getFile(String resourcePath) {
        return new File(XSSFTabularDataSourceSpec.getResource(resourcePath).toURI())
    }

}
