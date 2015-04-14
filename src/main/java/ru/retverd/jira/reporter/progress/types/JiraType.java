package ru.retverd.jira.reporter.progress.types;

import ru.retverd.jira.reporter.progress.adapters.ObjectList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

@XmlType(name = "jiraType")
@XmlAccessorType(XmlAccessType.FIELD)
public class JiraType {

    private String url;
    private boolean anonymous;
    private ProxyType proxy;
    @XmlSchemaType(name = "projectsType")
    @XmlJavaTypeAdapter(ObjectList.class)
    private List<String> projects;

    public String getUrl() {
        return url;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public ProxyType getProxy() {
        return proxy;
    }

    public List<String> getProjects() {
        return projects;
    }
}
