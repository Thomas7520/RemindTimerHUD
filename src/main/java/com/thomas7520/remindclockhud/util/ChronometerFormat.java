package com.thomas7520.remindclockhud.util;

import org.apache.commons.lang3.time.DurationFormatUtils;

public enum ChronometerFormat {

    S_MS("s.SSS"),
    SS_MS("ss.SSS"),
    MN_SS("mm:ss"),
    MN_SS_MS("mm:ss.SSS"),
    HH_MN_SS("HH:mm:ss"),
    HH_MN_SS_MS("HH:mm:ss.SSS"),
    DD_HH_MN_SS("dd:HH:mm:ss"),
    DD_HH_MN_SS_MS("dd:HH:mm:ss.SSS");

    private final String format;

    ChronometerFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public String formatTime(long milliseconds) {
        return DurationFormatUtils.formatDuration(Math.abs(System.currentTimeMillis() - milliseconds), format, true);
    }
}
