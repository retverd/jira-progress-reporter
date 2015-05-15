package ru.retverd.jira.reporter.progress.types;

import org.apache.poi.ss.util.CellReference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import ru.retverd.jira.reporter.progress.adapters.StringCell;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Locale;

@XmlType(name = "updateDateType")
@XmlAccessorType(XmlAccessType.NONE)
public class UpdateDateType {

    @XmlElement
    protected String timePattern;
    @XmlSchemaType(name = "string")
    @XmlJavaTypeAdapter(StringCell.class)
    protected CellType cell;

    public String getUpdateTime(DateTime dateTime, @Nullable Locale locale) {
        DateTimeFormatter dateFormForReport = DateTimeFormat.forPattern(timePattern);
        if (locale != null) {
            dateFormForReport = dateFormForReport.withLocale(locale);
        }
        return dateFormForReport.print(dateTime);
    }

    public CellReference getCell() {
        return cell == null ? null : cell.getCell();
    }
}