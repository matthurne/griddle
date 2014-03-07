package com.commercehub.griddle.poi.streaming

class SheetDataContainer {
    Map<Integer, String> headers;
    // Row Index -> 'Row' (Column index -> Value)
    Map<Integer, Map<Integer, String>> dataContainer;

    SheetDataContainer() {
        dataContainer = new TreeMap<Integer, Map<Integer, String>>();
    }

    public Map<Integer, String> getRow(Integer index) {
        return dataContainer.get(index);
    }

    public addReplaceRow(Integer rowIndex, Map<Integer, String> newRow) {
        dataContainer.put(rowIndex, newRow);
    }

    public setHeaders(Map<Integer, String> headers) {
        this.headers = headers;
    }

    public Map<Integer, String> getHeaders() {
        return headers;
    }
}
