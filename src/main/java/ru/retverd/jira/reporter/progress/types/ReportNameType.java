package ru.retverd.jira.reporter.progress.types;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reportNameType")
public class ReportNameType {

    protected String prefix = "";
    protected String timePattern = "";
    protected String suffix = "";

    public String getPrefix() {
        return prefix;
    }

    public String getTimePattern() {
        return timePattern;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getFullName(DateTime date) {
        DateTimeFormatter dateFormForReport = DateTimeFormat.forPattern(timePattern);
        String time = dateFormForReport.print(new DateTime());

        return prefix + time + suffix;
    }
}
