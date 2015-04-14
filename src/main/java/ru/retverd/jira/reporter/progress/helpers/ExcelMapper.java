package ru.retverd.jira.reporter.progress.helpers;

import org.apache.poi.ss.util.CellReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rtverdok on 13.04.2015.
 */
public class ExcelMapper {

    static public Map<CellMap, Integer> cellToIndices(String cell) {
        Map<CellMap, Integer> map = new HashMap<CellMap, Integer>();
        Integer row = null;
        Integer col = null;

        if (cell != null) {
            CellReference cf = new CellReference(cell);
            row = cf.getRow();
            col = (int) cf.getCol();
        }

        map.put(CellMap.ROW, row);
        map.put(CellMap.COL, col);

        return map;
    }
}
