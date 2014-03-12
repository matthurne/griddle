package com.commercehub.griddle.poi.streaming

class SheetDataContainerBackedRowIterator implements Iterator<Map<String, String>> {
    private final Map<Integer, String> transformedColumns
    private final Closure<String> valueTransformer
    private final Closure<Boolean> rowSkipCriteria

    private final SheetDataContainer backingData
    private final Iterator<Map.Entry<Integer, Map<Integer, String>>> delegateIterator
    private Map<Integer, String> nextRowValuesByColumnIndex

    SheetDataContainerBackedRowIterator(SheetDataContainer backingData, Map<Integer, String> transformedColumns,
                                        Closure<String> valueTransformer,
                                        Closure<Boolean> rowSkipCriteria) {
        this.backingData = backingData
        this.transformedColumns = transformedColumns
        this.valueTransformer = valueTransformer
        this.rowSkipCriteria = rowSkipCriteria

        this.delegateIterator = this.backingData.dataContainer.entrySet().iterator()

        if (transformedColumns) {
            readNextRow()
        } else {
            nextRowValuesByColumnIndex = null // If there aren't any defined columns, there will never be any meaningful row data
        }
    }

    @Override
    boolean hasNext() {
        return nextRowValuesByColumnIndex != null
    }

    // returns column name -> value
    @Override
    Map<String, String> next() {
        if (nextRowValuesByColumnIndex == null) {
            throw new NoSuchElementException()
        }
        def next = toExternalRow(nextRowValuesByColumnIndex)
        readNextRow()
        return next
    }

    @Override
    void remove() {
        throw new UnsupportedOperationException()
    }

    private void readNextRow() {
        nextRowValuesByColumnIndex = null
        while (delegateIterator.hasNext() && nextRowValuesByColumnIndex == null) {
            Map<Integer, String> next = delegateIterator.next().value
            if (!rowSkipCriteria(toExternalRow(next))) {
                nextRowValuesByColumnIndex = next
            }
        }
    }

    private Map<String, String> toExternalRow(Map<Integer, String> toTransform) {
        def externalRow = [:]
        for (entry in toTransform) {
            def columnName = transformedColumns.get(entry.key)
            if (columnName) {
                def val = valueTransformer(entry.value)
                externalRow[columnName] = val
            }
        }
        return externalRow
    }
}
