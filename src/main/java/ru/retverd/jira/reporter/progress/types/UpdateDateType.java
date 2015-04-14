package ru.retverd.jira.reporter.progress.types;

import org.apache.poi.ss.util.CellReference;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "updateDateType")
@XmlAccessorType(XmlAccessType.NONE)
public class UpdateDateType {

    @XmlElement
    protected String timePattern;
    protected int row;
    protected int col;

    public String getTimePattern() {
        return timePattern;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @XmlElement
    public void setCell(String value) {
        CellReference cf = new CellReference(value);
        row = cf.getRow();
        col = cf.getCol();
    }
}