package ru.retverd.jira.reporter.progress.types;

import org.apache.poi.ss.util.CellReference;

/**
 * Created by rtverdok on 15.05.2015.
 */
public class CellType {
    private CellReference cellRef;

    public CellType(String cellName) {
        cellRef = new CellReference(cellName);
    }

    public String getCellName() {
        String[] refParts = cellRef.getCellRefParts();
        String cellName = "";
        for (String refPart : refParts) {
            cellName = cellName + refPart;
        }
        return cellName;
    }

    public CellReference getCell() {
        return cellRef;
    }
}