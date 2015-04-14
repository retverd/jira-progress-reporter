package ru.retverd.jira.reporter.progress.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rtverdok on 14.04.2015.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class PropertiesLists {

    @XmlElement(name = "entry")
    protected List<String> list;

    public PropertiesLists() {

    }

    public PropertiesLists(List<String> entry) {
        list = new ArrayList<String>(entry);
    }

    public List<String> getList() {
        if (list == null) {
            list = new ArrayList<String>();
        }
        return this.list;
    }
}
