package ru.retverd.jira.reporter.progress.types;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.Locale;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reportNameType")
public class ReportNameType {

    protected String prefix = "";
    protected String timePattern = "";
    protected String suffix = "";

    public String getFullName(DateTime dateTime) {
        String time = "";
        if (!timePattern.isEmpty()) {
            DateTimeFormatter dateFormForReport = DateTimeFormat.forPattern(timePattern);
            time = dateFormForReport.withLocale(Locale.ENGLISH).print(dateTime);
        }
        return prefix + time + suffix;
    }
}
