package ru.retverd.jira.reporter.progress.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processingFlagsType")
public class ProcessingFlagsType {

    private boolean issueSummaryUpdate;
    private boolean recalculateFormulas;
    private boolean autosizeColumns;

    public boolean isIssueSummaryUpdate() {
        return issueSummaryUpdate;
    }

    public void setIssueSummaryUpdate(boolean value) {
        issueSummaryUpdate = value;
    }

    public boolean isRecalculateFormulas() {
        return recalculateFormulas;
    }

    public boolean isAutosizeColumns() {
        return autosizeColumns;
    }
}
