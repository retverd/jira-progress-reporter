package ru.retverd.jira.reporter.progress.adapters;

import org.apache.poi.ss.util.CellReference;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by rtverdok on 10.04.2015.
 */
public class ColumnIndex extends XmlAdapter<String, Integer> {
    @Override
    public String marshal(Integer column_index) {
        return CellReference.convertNumToColString(column_index);
    }

    @Override
    public Integer unmarshal(String column_name) {
        return CellReference.convertColStringToIndex(column_name);
    }
}
