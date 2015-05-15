package ru.retverd.jira.reporter.progress.types;

import org.apache.poi.ss.util.CellReference;
import ru.retverd.jira.reporter.progress.adapters.ObjectList;
import ru.retverd.jira.reporter.progress.adapters.StringCell;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "rootIssueType")
public class RootIssueType {

    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(StringCell.class)
    protected CellType issueKeyCell;
    @XmlSchemaType(name = "projectsType")
    @XmlJavaTypeAdapter(ObjectList.class)
    protected List<String> links;
    @XmlElement
    protected boolean unfoldSubtasks;

    public CellReference getCell() {
        return issueKeyCell == null ? null : issueKeyCell.getCell();
    }

    public List<String> getLinks() {
        return links;
    }

    public boolean isUnfoldSubtasks() {
        return unfoldSubtasks;
    }
}
