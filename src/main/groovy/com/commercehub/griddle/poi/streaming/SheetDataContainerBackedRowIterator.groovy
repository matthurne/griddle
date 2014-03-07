package com.commercehub.griddle.poi.streaming

class SheetDataContainerBackedRowIterator implements Iterator<Map<String, String>> {
    private final Map<Integer, String> transformedColumns
    private final Closure<String> valueTransformer
    private final ExcelCellElementMapper mapper;
    private final Closure<Boolean> rowSkipCriteria

    private final SheetDataContainer backingData
    private final Iterator<Map<Integer, String>> delegateIterator
    private Map<Integer, String> nextValue

    SheetDataContainerBackedRowIterator(SheetDataContainer backingData, Map<Integer, String> transformedColumns, Closure<String> valueTransformer, SimpleExcelCellElementMapper mapper,
                                        Closure<Boolean> rowSkipCriteria) {
        this.backingData = backingData
        this.transformedColumns = transformedColumns
        this.valueTransformer = valueTransformer
        this.mapper = mapper
        this.rowSkipCriteria = rowSkipCriteria

        this.delegateIterator = this.backingData.getDataContainer().entrySet().iterator();

        if (transformedColumns) {
            readNextRow()
        } else {
            nextValue = null // If there aren't any defined columns, there will never be any meaningful row data
        }
    }

    @Override
    boolean hasNext() {
        return nextValue != null
    }

    // returns column name -> value
    @Override
    Map<String, String> next() {
        if (nextValue == null) {
            throw new NoSuchElementException()
        }
        def next = toExternalRow(nextValue)
        readNextRow()
        return next
    }

    @Override
    void remove() {
        throw new UnsupportedOperationException()
    }

    private void readNextRow() {
        nextValue = null
        while (delegateIterator.hasNext() && nextValue == null) {
            Map<Integer, String> next = delegateIterator.next().value
            if (!rowSkipCriteria(toExternalRow(next))) {
                nextValue = next
            }
        }
    }

    private Map<String, String> toExternalRow(Map<Integer, String> toTransform) {
        def externalRow = [:]
        for (Map.Entry<Integer, String> entry : toTransform.entrySet().iterator()) {
            def columnName = transformedColumns.get(entry.key)
            if (columnName) {
                def val = valueTransformer(mapper.mapStringValue(entry.value));
                externalRow[columnName] = val
            }
        }
        return externalRow
    }
}
