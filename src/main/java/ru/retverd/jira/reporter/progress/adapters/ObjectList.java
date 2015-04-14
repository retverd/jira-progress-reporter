package ru.retverd.jira.reporter.progress.adapters;

import ru.retverd.jira.reporter.progress.types.PropertiesLists;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.List;

/**
 * Created by rtverdok on 14.04.2015.
 */
public class ObjectList extends XmlAdapter<PropertiesLists, List<String>> {
    @Override
    public PropertiesLists marshal(List<String> list) {
        return new PropertiesLists(list);
    }

    @Override
    public List<String> unmarshal(PropertiesLists object) {
        return object.getList();
    }
}
