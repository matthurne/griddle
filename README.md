# Overview

Griddle is a [Groovy](http://groovy.codehaus.org) library for easily processing tabular data.  Currently, it supports Excel files (both HSSF and XSSF) and CSV files.

# Usage

First, add a dependency to your build file.  Releases are published to [Bintray JCenter](https://bintray.com/bintray/jcenter).  See the [change log](CHANGES.md) for the latest version.

Gradle:

```groovy
repositories {
    jcenter()
}
dependencies {
    compile "com.commercehub:griddle:1.0.0"
}
```

Maven:

```xml
<dependency>
    <groupId>com.commercehub</groupId>
    <artifactId>griddle</artifactId>
    <version>1.0.0</version>
</dependency>
```

Next, create the desired instance of `TabularDataSource`.

```groovy
def tabularDataSource = new CSVTabularDataSource()
// OR
def tabularDataSource = new HSSFTabularDataSource()
// OR
def tabularDataSource = new XSSFTabularDataSource()
```

Then use the `TabularDataSource` to process files.  The `withFile` method is used to provide access to `TabularData` objects processed from a file.  Each `TabularData` object provides access to the names of the columns in that table, as well as the rows in the table (represented as a `Map` of `String`, column name to cell value).  The file is automatically closed at the end of the closure passed to `withFile`.

```groovy
def file = new File("myfile.xlsx")
tabularDataSource.withFile(file) { Iterable<TabularData> tables ->
    for (table in tables) {
        println "Processing a table with columns ${table.columnNames}..."
        for (row in table.rows) {
            println "Row: ${row}"
        }
        println "Completed table processing."
    }
}
```

# Extension/Customization

## ExcelCellMapper

Excel cells are actually rather complex.  They have different types, can contain formulas, have styles, have a presentation format, etc.  To control how [Apache POI](http://poi.apache.org/) `Cell`s are converted into `String`s, implement the `ExcelCellMapper` interface and provide an instance to the `HSSFTabularDataSource`/`XSSFTabularDataSource` constructor.

## Row Skip Criteria

Sometimes, it may be useful to ignore certain rows in the file.  To do this, when calling the `getRows` method on `TabularDataSource`, pass a closure defining your row skip criteria.  This closure is passed the row `Map` as the argument, and should return `true` if the row should be skipped.
