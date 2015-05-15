package ru.retverd.jira.reporter.progress.helpers;

import org.apache.poi.ss.util.CellReference;

/**
 * Created by rtverdok on 15.05.2015.
 */
public class ExcelHelper {
    static public int humanizeRow(int row) {
        return row + 1;
    }

    static public String humanizeColumn(int column) {
        return CellReference.convertNumToColString(column);
    }
}
