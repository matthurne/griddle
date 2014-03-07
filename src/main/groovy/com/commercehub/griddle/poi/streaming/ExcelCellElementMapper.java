package com.commercehub.griddle.poi.streaming;

interface ExcelCellElementMapper {
    String mapStringValue(String value);
    boolean shouldCaclulateFormulas();
}
