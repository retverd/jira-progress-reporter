package ru.retverd.jira.reporter.progress.tests;

import ru.retverd.jira.reporter.progress.Starter;

public class DebugRun {
    public static void main(String[] args) throws Exception {
        String[] argsForStarter = new String[]{"default.properties", "default.xlsx", "username", "password"};

        Starter.main(argsForStarter);
    }
}
