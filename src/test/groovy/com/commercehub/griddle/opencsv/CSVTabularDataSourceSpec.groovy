package com.commercehub.griddle.opencsv

import com.commercehub.griddle.TabularData
import spock.lang.Specification

class CSVTabularDataSourceSpec extends Specification {

    private static final File FILE1 = getFile("/csv/csv1.csv")

    def tabularDataSource = new CSVTabularDataSource()

    def "withFile produces one table per file"() {
        int actualTableCount = -1

        when:
        tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
            actualTableCount = tables.collect().size()
        }

        then:
        actualTableCount == expectedTableCount

        where:
        file  | expectedTableCount
        FILE1 | 1
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
        file  | expectedColumnNames
        FILE1 | ["Foo", "Bar", "Moo", "Goo"]
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
            file  | expectedRows
            FILE1 | [["Foo":"Lorem", "Bar":"ipsum", "Moo":"dolor", "Goo":"sit"], ["Foo":"12.1", "Bar":"amet", "Moo":"consectetur", "Goo":"7"], ["Foo":"adipisicing", "Bar":"1000", "Moo":"22.1241", "Goo":"elit, sed do"]]
    }

    private static File getFile(String resourcePath) {
        return new File(CSVTabularDataSource.getResource(resourcePath).toURI())
    }
}
