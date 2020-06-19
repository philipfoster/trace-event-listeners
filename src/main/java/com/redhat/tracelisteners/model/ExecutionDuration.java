package com.redhat.tracelisteners.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"Hours", "Minutes", "Seconds", "Milliseconds"})
public class ExecutionDuration {

    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private long milliseconds = 0L;

	@JsonProperty("Hours")
    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

	@JsonProperty("Minutes")
    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

	@JsonProperty("Seconds")
    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

	@JsonProperty("Milliseconds")
    public long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    @Override
    public String toString() {
        return "Exeuction  Duration hours=" + getHours() + ", minutes=" + getMinutes() + ", seconds=" + getSeconds() + ", milliseconds=" + getMilliseconds();
    }
}