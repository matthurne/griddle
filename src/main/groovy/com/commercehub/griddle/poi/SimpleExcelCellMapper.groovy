package com.commercehub.griddle.poi

import org.apache.poi.ss.usermodel.Cell

/**
 * A simple implementation of ExcelCellMapper that returns values only for string cells and string formulas.
 */
class SimpleExcelCellMapper implements ExcelCellMapper {

    @Override
    String mapCell(Cell cell) {
        try {
            return cell.stringCellValue ?: ""
        } catch (IllegalStateException ignored) {
            return ""
        }
    }

}
