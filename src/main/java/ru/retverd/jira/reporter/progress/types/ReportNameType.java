package ru.retverd.jira.reporter.progress.types;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nullable;
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

    public String getFullName(DateTime dateTime, @Nullable Locale locale) {
        String time = "";
        if (!timePattern.isEmpty()) {
            DateTimeFormatter dateFormForReport = DateTimeFormat.forPattern(timePattern);
            if (locale != null) {
                dateFormForReport = dateFormForReport.withLocale(locale);
            }
            time = dateFormForReport.print(dateTime);
        }
        return prefix + time + suffix;
    }
}
