package ru.retverd.jira.reporter.progress.types;

import ru.retverd.jira.reporter.progress.adapters.StringLocale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Locale;

@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigType {

    private JiraType jira;
    private ReportType report;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(StringLocale.class)
    private Locale locale;

    public JiraType getJira() {
        return jira;
    }

    public ReportType getReport() {
        return report;
    }

    public Locale getLocale() {
        return locale;
    }
}
