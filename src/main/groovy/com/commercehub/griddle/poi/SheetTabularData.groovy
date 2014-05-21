package com.commercehub.griddle.poi

import com.commercehub.griddle.TabularData

import org.apache.poi.ss.usermodel.Sheet

class SheetTabularData implements TabularData {

    private final Sheet sheet
    private final Closure<String> valueTransformer
    private final ExcelCellMapper cellMapper
    private final Map<Integer, String> transformedColumnNamesByIndex
	private final List<String> transformedColumnNames

    SheetTabularData(Sheet sheet, Closure<String> columnNameTransformer, Closure<String> valueTransformer,
                     ExcelCellMapper cellMapper) {
        this.sheet = sheet
        this.valueTransformer = valueTransformer
        this.cellMapper = cellMapper
        def headerRow = sheet.getRow(0)
		
		transformedColumnNamesByIndex = [:]
		transformedColumnNames = []
		
		headerRow?.each {
			def transformedColumnName = columnNameTransformer(cellMapper.mapCell(it))
			if (transformedColumnName) {
				transformedColumnNamesByIndex << [(it.columnIndex):transformedColumnName]
				transformedColumnNames << transformedColumnName
			}
		}
    }

    @Override
    List<String> getColumnNames() {
        return Collections.unmodifiableList(transformedColumnNames)
    }

    @Override
    Iterable<Map<String, String>> getRows() {
        return getRows(NEVER_SKIP)
    }

    @Override
    Iterable<Map<String, String>> getRows(Closure<Boolean> rowSkipCriteria) {
        return {
            new RowIterator(sheet, transformedColumnNamesByIndex, valueTransformer, cellMapper, rowSkipCriteria)
        } as Iterable<Map<String, String>>
    }

}
