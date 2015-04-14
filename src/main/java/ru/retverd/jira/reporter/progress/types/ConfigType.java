package ru.retverd.jira.reporter.progress.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigType {

    private JiraType jira;
    private ReportType report;

    public JiraType getJira() {
        return jira;
    }

    public ReportType getReport() {
        return report;
    }
}
