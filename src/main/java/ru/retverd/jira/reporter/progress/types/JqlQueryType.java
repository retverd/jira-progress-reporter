package ru.retverd.jira.reporter.progress.types;

import org.apache.poi.ss.util.CellReference;
import ru.retverd.jira.reporter.progress.adapters.StringCell;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "jqlQueryType")
public class JqlQueryType {

    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(StringCell.class)
    protected CellType cell;
    @XmlSchemaType(name = "positiveInteger")
    private int searchStep;

    public CellReference getCell() {
        return cell == null ? null : cell.getCell();
    }

    public int getSearchStep() {
        return searchStep;
    }
}