package com.commercehub.griddle.poi

import org.apache.poi.ss.usermodel.Cell

interface ExcelCellMapper {

    String mapCell(Cell cell)

}