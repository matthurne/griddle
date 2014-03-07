package com.commercehub.griddle.poi.streaming


class SimpleExcelCellElementMapper implements ExcelCellElementMapper {
    @Override
    boolean shouldCaclulateFormulas() {
        return true;
    }

    @Override
    String mapStringValue(String value) {
        return value;
    }
}
