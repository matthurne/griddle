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
    compile "com.commercehub:griddle:3.0.0"
}
```

Maven:

```xml
<dependency>
    <groupId>com.commercehub</groupId>
    <artifactId>griddle</artifactId>
    <version>3.0.0</version>
</dependency>
```

Next, create the desired instance of `TabularDataSource`.

Please note that `com.commercehub.griddle.opencsv.CSVTabularDataSource` is deprecated; use `com.commercehub.griddle.supercsv.CSVTabularDataSource` instead.

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

# Development

## Releasing
Releases are uploaded to [Bintray](https://bintray.com/) via the
[gradle-release](https://github.com/townsfolk/gradle-release) plugin and
[gradle-bintray-plugin](https://github.com/bintray/gradle-bintray-plugin). To upload a new release, you need to be a
member of the [commercehub-oss Bintray organization](https://bintray.com/commercehub-oss). You need to specify your
Bintray username and API key when uploading. Your API key can be found on your
[Bintray user profile page](https://bintray.com/profile/edit). You can put your username and API key in
`~/.gradle/gradle.properties` like so:

    bintrayUserName = johndoe
    bintrayApiKey = 0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef

Then, to upload the release:

    ./gradlew release

Alternatively, you can specify your Bintray username and API key on the command line:

    ./gradlew -PbintrayUserName=johndoe -PbintrayApiKey=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef release

The `release` task will prompt you to enter the version to be released, and will create and push a release tag for the
specified version. It will also upload the release artifacts to Bintray.

After the release artifacts have been uploaded to Bintray, they must be published to become visible to users. See
Bintray's [Publishing](https://bintray.com/docs/uploads/uploads_publishing.html) documentation for instructions.

After publishing the release on Bintray, it's also nice to create a GitHub release. To do so:
*   Visit the project's [releases](https://github.com/commercehub-oss/griddle/releases) page
*   Click the "Draft a new release" button
*   Select the tag that was created by the Gradle `release` task
*   Enter a title; typically, this should match the tag (e.g. "1.2.0")
*   Enter a description of what changed since the previous release (see the
    [changelog](https://github.com/commercehub-oss/griddle/blob/master/CHANGES.md))
*   Click the "Publish release" button
