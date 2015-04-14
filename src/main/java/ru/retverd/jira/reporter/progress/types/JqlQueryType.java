package ru.retverd.jira.reporter.progress.types;

import org.apache.poi.ss.util.CellReference;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "jqlQueryType")
public class JqlQueryType {

    private int row;
    private int col;
    @XmlSchemaType(name = "positiveInteger")
    private int searchStep;

    @XmlElement
    public void setCell(String value) {
        CellReference cf = new CellReference(value);
        row = cf.getRow();
        col = cf.getCol();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getSearchStep() {
        return searchStep;
    }
}