package org.sudo.tools.models;

import lombok.Builder;

@Builder
public class AssEvent {
    String labelName;
    @Builder.Default
    int layer = 0;
    @Builder.Default
    double startTimeStamp = 0;
    @Builder.Default
    double endTimeStamp = 0;
    String style;
    String name;
    @Builder.Default
    int marginL = 0;
    @Builder.Default
    int marginR = 0;
    @Builder.Default
    int marginV = 0;
    @Builder.Default
    String effect = "";
    String text;

    private static String formatTimeStamp(double timeStamp) {
        double timeStampNoHour = timeStamp % 3600;
        double seconds = timeStampNoHour % 60;

        int hour = (int) ((timeStamp - timeStampNoHour) / 3600);
        int minute = (int) ((timeStampNoHour - seconds) / 60);

        return String.format("%d:%02d:%02.02f", hour, minute, seconds);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder
                .append(this.labelName)
                .append(": ")
                .append(this.layer)
                .append(',')
                .append(formatTimeStamp(this.startTimeStamp))
                .append(',')
                .append(formatTimeStamp(this.endTimeStamp))
                .append(',')
                .append(this.style)
                .append(',')
                .append(this.name)
                .append(',')
                .append(marginL)
                .append(',')
                .append(marginR)
                .append(',')
                .append(marginV)
                .append(',')
                .append(this.effect)
                .append(',')
                .append(this.text)
                .toString();
    }
}
