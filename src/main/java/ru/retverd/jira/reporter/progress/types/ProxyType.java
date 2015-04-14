package ru.retverd.jira.reporter.progress.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "proxyType")
public class ProxyType {

    protected String host;
    protected String port;

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }
}
