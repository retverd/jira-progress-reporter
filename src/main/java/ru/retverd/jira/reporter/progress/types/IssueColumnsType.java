package ru.retverd.jira.reporter.progress.types;

import ru.retverd.jira.reporter.progress.adapters.ColumnIndex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "issueColumnsType")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueColumnsType {

    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer summary;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer key;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer relation;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer parentKey;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer estimation;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer spentTime;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer remainingTime;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer status;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer dueDate;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer afVersion;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer components;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer labels;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(ColumnIndex.class)
    private Integer assignee;

    public Integer getSummary() {
        return summary;
    }

    public Integer getKey() {
        return key;
    }

    public Integer getRelation() {
        return relation;
    }

    public Integer getParentKey() {
        return parentKey;
    }

    public Integer getEstimation() {
        return estimation;
    }

    public Integer getSpentTime() {
        return spentTime;
    }

    public Integer getRemainingTime() {
        return remainingTime;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getDueDate() {
        return dueDate;
    }

    public Integer getAfVersion() {
        return afVersion;
    }

    public Integer getComponents() {
        return components;
    }

    public Integer getLabels() {
        return labels;
    }

    public Integer getAssignee() {
        return assignee;
    }
}