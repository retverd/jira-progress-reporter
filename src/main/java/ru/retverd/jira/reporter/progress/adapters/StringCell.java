package ru.retverd.jira.reporter.progress.adapters;

import ru.retverd.jira.reporter.progress.types.CellType;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by rtverdok on 10.04.2015.
 */
public class StringCell extends XmlAdapter<String, CellType> {
    @Override
    public String marshal(CellType cell) {
        return cell.getCellName();
    }

    @Override
    public CellType unmarshal(String cell) {
        if (cell == null) {
            return null;
        } else {
            return new CellType(cell);
        }
    }
}
