package ru.retverd.jira.reporter.progress.types;

import org.apache.poi.ss.util.CellReference;
import ru.retverd.jira.reporter.progress.adapters.ObjectList;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "rootIssueType")
public class RootIssueType {

    protected int issueKeyRow;
    protected int issueKeyCol;
    @XmlSchemaType(name = "projectsType")
    @XmlJavaTypeAdapter(ObjectList.class)
    protected List<String> links;
    @XmlElement
    protected boolean unfoldSubtasks;

    @XmlElement
    public void setIssueKeyCell(String value) {
        CellReference cf = new CellReference(value);
        issueKeyRow = cf.getRow();
        issueKeyCol = cf.getCol();
    }

    public int getIssueKeyRow() {
        return issueKeyRow;
    }

    public int getIssueKeyCol() {
        return issueKeyCol;
    }

    public List<String> getLinks() {
        return links;
    }

    public boolean isUnfoldSubtasks() {
        return unfoldSubtasks;
    }
}
