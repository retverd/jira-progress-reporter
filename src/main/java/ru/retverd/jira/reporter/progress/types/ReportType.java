package ru.retverd.jira.reporter.progress.types;

import javax.xml.bind.annotation.*;

@XmlType(name = "reportType")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportType {

    protected String marker;
    protected UpdateDateType updateDate;
    protected ToHideType toHide;
    protected JqlQueryType jqlQuery;
    protected RootIssueType rootIssue;
    @XmlTransient
    protected int startProcessingRow;
    protected IssueColumnsType issueColumns;
    protected ProcessingFlagsType processingFlags;
    protected ReportNameType reportName;

    public String getMarker() {
        return marker;
    }

    public UpdateDateType getUpdateDate() {
        return updateDate;
    }

    public ToHideType getToHide() {
        return toHide;
    }

    public JqlQueryType getJqlQuery() {
        return jqlQuery;
    }

    public void setJqlQuery(JqlQueryType value) {
        this.jqlQuery = value;
    }

    public RootIssueType getRootIssue() {
        return rootIssue;
    }

    public int getStartProcessingRow() {
        return startProcessingRow;
    }

    @XmlElement
    public void setStartProcessingRow(int value) {
        startProcessingRow = value - 1;
    }

    public IssueColumnsType getIssueColumns() {
        return issueColumns;
    }

    public ProcessingFlagsType getProcessingFlags() {
        return processingFlags;
    }

    public ReportNameType getReportName() {
        return reportName;
    }
}
