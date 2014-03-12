package com.commercehub.griddle.poi.streaming

class SheetDataContainer {
    Map<Integer, String> headers
    // Row Index -> 'Row' (Column index -> Value)
    Map<Integer, Map<Integer, String>> dataContainer

    SheetDataContainer() {
        dataContainer = new TreeMap<Integer, Map<Integer, String>>()
    }

    void addReplaceRow(Integer rowIndex, Map<Integer, String> newRow) {
        dataContainer.put(rowIndex, newRow)
    }

}
