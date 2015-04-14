package ru.retverd.jira.reporter.progress.tests;

import ru.retverd.jira.reporter.progress.Starter;

public class DebugRun {
    public static void main(String[] args) throws Throwable {
        String[] argsForStarter = new String[]{"default_config.xml", "default.xlsx", "username", "password"};

        Starter.main(argsForStarter);
    }
}
