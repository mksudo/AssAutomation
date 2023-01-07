package org.sudo.tools.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogArea {
    private final List<String> logs;
    private final boolean isFixed;
    private boolean isFrozen;
    private String frozenText;

    public LogArea() {
        this.logs = new ArrayList<>();
        this.isFixed = false;
        this.isFrozen = false;
    }

    public LogArea(int fixedLogCount) {
        String[] fixedLogs = new String[fixedLogCount];
        Arrays.fill(fixedLogs, "");
        this.logs = Arrays.asList(fixedLogs);
        this.isFixed = true;
        this.isFrozen = false;
    }

    public void addLog(String log) {
        this.logs.add(log);
    }

    public void addLogFixed(String log, int logIndex) {
        this.logs.set(logIndex, log);
    }

    public String getLog() {
        return this.isFrozen ? this.frozenText : String.join("\n", this.logs);
    }

    public void freeze() {
        this.frozenText = this.getLog();
        this.isFrozen = true;
    }
}
