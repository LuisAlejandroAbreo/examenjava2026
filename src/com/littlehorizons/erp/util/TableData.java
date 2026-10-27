package com.littlehorizons.erp.util;

import java.util.ArrayList;
import java.util.List;

/** Generic tabular result (headers + rows) used to print joined queries without extra model classes. */
public class TableData {

    private final List<String> headers = new ArrayList<>();
    private final List<Object[]> rows = new ArrayList<>();

    public List<String> getHeaders() {
        return headers;
    }

    public List<Object[]> getRows() {
        return rows;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public int size() {
        return rows.size();
    }
}
