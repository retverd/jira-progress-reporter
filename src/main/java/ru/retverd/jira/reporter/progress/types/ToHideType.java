package ru.retverd.jira.reporter.progress.types;

import ru.retverd.jira.reporter.progress.helpers.CellMap;
import ru.retverd.jira.reporter.progress.helpers.ExcelMapper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Map;

@XmlType(name = "toHideType")
@XmlAccessorType(XmlAccessType.NONE)
public class ToHideType {

    protected Integer issuePrefixRow;
    protected Integer issuePrefixCol;
    protected Integer affectedVersionRow;
    protected Integer affectedVersionCol;
    protected Integer componentsRow;
    protected Integer componentsCol;
    protected Integer labelsRow;
    protected Integer labelsCol;

    @XmlElement
    protected void setIssuePrefixCell(String value) {
        Map<CellMap, Integer> map = ExcelMapper.cellToIndices(value);

        issuePrefixRow = map.get(CellMap.ROW);
        issuePrefixCol = map.get(CellMap.COL);
    }

    public Integer getIssuePrefixRow() {
        return issuePrefixRow;
    }

    public Integer getIssuePrefixCol() {
        return issuePrefixCol;
    }

    @XmlElement(required = true)
    protected void setAffectedVersionCell(String value) {
        Map<CellMap, Integer> map = ExcelMapper.cellToIndices(value);

        affectedVersionRow = map.get(CellMap.ROW);
        affectedVersionCol = map.get(CellMap.COL);
    }

    public Integer getAffectedVersionRow() {
        return affectedVersionRow;
    }

    public Integer getAffectedVersionCol() {
        return affectedVersionCol;
    }

    @XmlElement(required = true)
    protected void setComponentsCell(String value) {
        Map<CellMap, Integer> map = ExcelMapper.cellToIndices(value);

        componentsRow = map.get(CellMap.ROW);
        componentsCol = map.get(CellMap.COL);
    }

    public Integer getComponentsRow() {
        return componentsRow;
    }

    public Integer getComponentsCol() {
        return componentsCol;
    }

    @XmlElement(required = true)
    protected void setLabelsCell(String value) {
        Map<CellMap, Integer> map = ExcelMapper.cellToIndices(value);

        labelsRow = map.get(CellMap.ROW);
        labelsCol = map.get(CellMap.COL);
    }

    public Integer getLabelsRow() {
        return labelsRow;
    }

    public Integer getLabelsCol() {
        return labelsCol;
    }
}
