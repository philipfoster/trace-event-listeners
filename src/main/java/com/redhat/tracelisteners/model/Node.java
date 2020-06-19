package com.redhat.tracelisteners.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.redhat.tracelisteners.marshalling.LocalDateTimeDeserializer;
import com.redhat.tracelisteners.marshalling.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@JsonPropertyOrder({"ID", "StartedOn", "CompletedOn", "State", "Type", "Name", "DecisionServices"})
public class Node {
    
    private String ID;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startedOn;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime completedOn;

    private NodeState state = NodeState.None;
    private NodeType type = NodeType.None;
    private List<DecisionServiceTrace> decisionServices = new ArrayList<>( );
    private String name;

	@JsonProperty("ID")
    public String getID() {
        return this.ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

	@JsonProperty("StartedOn")
    public LocalDateTime getStartedOn() {
        return this.startedOn;
    }

    public void setStartedOn(LocalDateTime startedOn) {
        this.startedOn = startedOn;
    }

	@JsonProperty("CompletedOn")
    public LocalDateTime getCompletedOn() {
        return this.completedOn;
    }

    public void setCompletedOn(LocalDateTime completedOn) {
        this.completedOn = completedOn;
    }

	@JsonProperty("State")
    public void setState(NodeState state) {
        this.state = state;
    }

    public NodeState getState() {
        return this.state;
    }

    @JsonProperty("Type")
    public void setType(NodeType type) {
        this.type = type;
    }

    public NodeType getType() {
        return this.type;
    }

    @JsonProperty("Name")
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

	@JsonProperty("DecisionServices")
    public List<DecisionServiceTrace>  getDecisionServices() {
        return this.decisionServices;
    }

    public void setDecisionServices(List<DecisionServiceTrace> decisionServices) {
        this.decisionServices = decisionServices;
    }

    @Override
    public String toString() {
        return "Node ID=" + getID() + ", startedOn=" + getStartedOn() + ", completedOn=" + getCompletedOn() + ", state=" + getState() + ", type=" + getType() + ", numberOfDecisionServices=" + getDecisionServices().size();
    }
}