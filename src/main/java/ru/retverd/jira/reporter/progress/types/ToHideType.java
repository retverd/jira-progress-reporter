package ru.retverd.jira.reporter.progress.types;

import org.apache.poi.ss.util.CellReference;
import ru.retverd.jira.reporter.progress.adapters.StringCell;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "toHideType")
@XmlAccessorType(XmlAccessType.NONE)
public class ToHideType {

    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(StringCell.class)
    protected CellType issuePrefixCell;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(StringCell.class)
    protected CellType affectedVersionCell;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(StringCell.class)
    protected CellType componentsCell;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(StringCell.class)
    protected CellType labelsCell;

    public CellReference getIssuePrefixCell() {
        return issuePrefixCell == null ? null : issuePrefixCell.getCell();
    }

    public CellReference getAffectedVersionCell() {
        return affectedVersionCell == null ? null : affectedVersionCell.getCell();
    }

    public CellReference getComponentsCell() {
        return componentsCell == null ? null : componentsCell.getCell();
    }

    public CellReference getLabelsCell() {
        return labelsCell == null ? null : labelsCell.getCell();
    }
}
