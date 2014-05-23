package com.commercehub.griddle.opencsv

import com.commercehub.griddle.TabularData
import spock.lang.Specification

class CSVTabularDataSourceSpec extends Specification {

    private static final File FILE1 = getFile("/csv/csv1.csv")
    private static final File FILE_WITH_MISSING_COLUMN_NAME = getFile("/csv/csv-with-missing-column-name.csv")

    def tabularDataSource = new CSVTabularDataSource()

    def "withFile produces one table per file"() {
        int actualTableCount = -1

        when:
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            actualTableCount = tables.collect().size()
        }

        then:
        actualTableCount == 1

        where:
        file                            | _
        FILE1                           | _
        FILE_WITH_MISSING_COLUMN_NAME   | _
    }

    @SuppressWarnings("LineLength")
    def "tables have expected columnNames"() {
        def actualColumnNames = null

        when:
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            actualColumnNames = tables.first().columnNames.collect()
        }

        then:
        actualColumnNames == expectedColumnNames

        where:
        file                            | expectedColumnNames
        FILE1                           | ["Foo", "Bar", "Moo", "Goo"]
        FILE_WITH_MISSING_COLUMN_NAME   | ["Column 1", "Column 2", "Column 4"]
    }

    @SuppressWarnings("LineLength")
    def "table rows have expected values"() {
        def actualRows = null

        when:
            tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
                actualRows = tables.first().rows.collect()
            }

        then:
            actualRows == expectedRows

        where:
            file                            | expectedRows
            FILE1                           | [["Foo":"Lorem", "Bar":"ipsum", "Moo":"dolor", "Goo":"sit"], ["Foo":"12.1", "Bar":"amet", "Moo":"consectetur", "Goo":"7"], ["Foo":"adipisicing", "Bar":"1000", "Moo":"22.1241", "Goo":"elit, sed do"]]
            FILE_WITH_MISSING_COLUMN_NAME   | [["Column 1":"Row 1 Column 1", "Column 4":"Row 1 Column 4"], ["Column 1":"Row 3 Column 1", "Column 2":"Row 3 Column 2", "Column 4":"Row 3 Column 4"], ["Column 1":"Row 4 Column 1", "Column 2":"Row 4 Column 2", "Column 4":"Row 4 Column 4"]]
    }

    private static File getFile(String resourcePath) {
        return new File(CSVTabularDataSource.getResource(resourcePath).toURI())
    }
}
