# Overview

Griddle is a [Groovy](http://groovy.codehaus.org) library for easily processing tabular data.  Currently, it supports Excel files (both HSSF and XSSF) and CSV files.

[![Build Status](https://travis-ci.org/commercehub-oss/griddle.png?branch=master)](https://travis-ci.org/commercehub-oss/griddle)

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
// OR
def tabularDataSource = new StreamingXSSFTabularDataSource()
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

## Column Name / Value Transformer

Sometimes, the column names or values that you want when processing aren't exactly what is present in your file.  For example, you may want to trim whitespace from data cell values, or treat column names in a case-insensitive manner.  To accomplish this, set a transformer using the `setColumnNameTransformer` or `setValueTransformer` methods on `TabularDataSource`.  For common cases, you can use pre-written transformers provided by the `Transformers` class.

## ExcelCellMapper

Excel cells are actually rather complex.  They have different types, can contain formulas, have styles, have a presentation format, etc.  To control how [Apache POI](http://poi.apache.org/) `Cell`s are converted into `String`s, implement the `ExcelCellMapper` interface and provide an instance to the `HSSFTabularDataSource`/`XSSFTabularDataSource` constructor.  Note, the `StreamingXSSFTabularDataSource` does not yet support cell mapping using this mechanism.

## Row Skip Criteria

Sometimes, it may be useful to ignore certain rows in the file.  To do this, when calling the `getRows` method on `TabularDataSource`, pass a closure defining your row skip criteria.  This closure is passed the row `Map` as the argument, and should return `true` if the row should be skipped.

## Modifying formatting when using StreamingXSSFTabularDataSource

The `StreamingXSSFTabularDataSource` relies on `org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler` to format output, which provides for minimal formatting extension points.  Overriding the handlerBuilder method of `StreamingXSSFTabularData` to return a customized `org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler` is the only current mechanism to manipulate the default formatting/mapping behavior.
