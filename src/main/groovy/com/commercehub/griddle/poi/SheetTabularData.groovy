package com.commercehub.griddle.poi

import com.commercehub.griddle.TabularData
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType
import org.apache.poi.ss.usermodel.Sheet

class SheetTabularData implements TabularData {

    private final Sheet sheet
    private final Closure<String> valueTransformer
    private final ExcelCellMapper cellMapper
    private final Map<Integer, String> transformedColumnNamesByIndex
    private final List<String> transformedColumnNames

    SheetTabularData(Sheet sheet,
                     @ClosureParams(value=SimpleType, options="java.lang.String") Closure<String> columnNameTransformer,
                     @ClosureParams(value=SimpleType, options="java.lang.String") Closure<String> valueTransformer,
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
                transformedColumnNamesByIndex[it.columnIndex] = transformedColumnName
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
    Iterable<Map<String, String>> getRows(@ClosureParams(value=FromString, options="java.util.Map<java.lang.String, java.lang.String>")
                                                  Closure<Boolean> rowSkipCriteria) {

        return {
            new RowIterator(sheet, transformedColumnNamesByIndex, valueTransformer, cellMapper, rowSkipCriteria)
        } as Iterable<Map<String, String>>
    }

}
