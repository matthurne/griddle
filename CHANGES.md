# Change Log
*   1.2.1
    *   POI: only apply cell mappings/transformations once per row. [#4](https://github.com/commercehub-oss/griddle/issues/4)

*   1.2.0
    *   Upgrade to POI 3.10-FINAL.
    *   Add support for a stream based Excel reader/parser, useful for lower memory footprints.

*   1.1.2
    *   Properly populates contents of published source JARs.
    *   No longer publishes Javadoc JARs.

*   1.1.1
    *   Fixes a `NullPointerException` when processing Excel workbooks with empty worksheets.

*   1.1.0
    *   Adds support for column name transformers
    *   Adds support for value transformers

*   1.0.0
    *   Initial version
    *   Adds support for Excel (HSSF and XSSF), including custom cell mapping
    *   Adds support for CSV
    *   Adds support for custom row skip criteria
