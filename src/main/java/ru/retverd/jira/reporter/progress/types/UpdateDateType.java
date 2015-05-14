package ru.retverd.jira.reporter.progress.types;

import org.apache.poi.ss.util.CellReference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Locale;

@XmlType(name = "updateDateType")
@XmlAccessorType(XmlAccessType.NONE)
public class UpdateDateType {

    @XmlElement
    protected String timePattern;
    protected int row;
    protected int col;

    public String getUpdateTime(DateTime dateTime, @Nullable Locale locale) {
        DateTimeFormatter dateFormForReport = DateTimeFormat.forPattern(timePattern);
        if (locale != null) {
            dateFormForReport = dateFormForReport.withLocale(locale);
        }
        return dateFormForReport.print(dateTime);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @XmlElement
    public void setCell(String value) {
        CellReference cf = new CellReference(value);
        row = cf.getRow();
        col = cf.getCol();
    }
}