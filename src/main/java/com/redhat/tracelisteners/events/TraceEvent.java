package com.redhat.tracelisteners.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;

@JsonPropertyOrder({"ID", "TimeStamp", "EventType"})
public class TraceEvent {

	private String ID;
	private LocalDateTime timestamp;
	private TraceEventType eventType = TraceEventType.None;

	@JsonProperty("ID")
	public String getID() {
		return this.ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	@JsonProperty("TimeStamp")
	public LocalDateTime getTimeStamp() {
		return this.timestamp;
	}

	public void setTimeStamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	@JsonProperty("EventType")
	public TraceEventType getEventType() {
		return this.eventType;
	}

	public void setEventType(TraceEventType eventType) {
		this.eventType = eventType;
	}
}
