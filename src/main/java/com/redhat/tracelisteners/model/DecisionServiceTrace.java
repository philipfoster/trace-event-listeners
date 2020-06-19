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


@JsonPropertyOrder({"ID", "CorrelationID", "StartedOn", "CompletedOn", "AllRules", "RulesFired", "RulesNotFired", "TotalRuleCount", "RulesFiredCount", "RulesNotFiredCount", "ExecutionDuration"})
public class DecisionServiceTrace {

    private String ID;
    private String correlationID;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startedOn;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime completedOn;
    private List<Rule> allRules = new ArrayList<>( );
    private List<Rule> rulesFired = new ArrayList<>( );
    private List<Rule> rulesNotFired = new ArrayList<>( );
    private int totalRuleCount = 0;
    private int rulesFiredCount = 0;
    private int rulesNotFiredCount = 0;
    private ExecutionDuration executionDuration;

	 @JsonProperty("ID")
    public String getID() {
        return this.ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    @JsonProperty("CorrelationID")
    public String getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
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

	@JsonProperty("RulesFired")
    public List<Rule> getRulesFired() {
        return this.rulesFired;
    }

    public void setRulesFired(List<Rule> rulesFired) {
        this.rulesFired = rulesFired;
    }

	@JsonProperty("RulesNotFired")
    public List<Rule> getRulesNotFired() {
        return this.rulesNotFired;
    }

    public void setRulesNotFired(List<Rule> rulesNotFired) {
        this.rulesNotFired = rulesNotFired;
    }

	@JsonProperty("AllRules")
    public List<Rule> getAllRules() {
        return this.allRules;
    }

    public void setAllRules(List<Rule> allRules) {
        this.allRules = allRules;
    }

	@JsonProperty("TotalRuleCount")
    public int getTotalRuleCount() {
        return this.totalRuleCount;
    }

    public void setTotalRuleCount(int totalRuleCount) {
        this.totalRuleCount = totalRuleCount;
    }

	@JsonProperty("RulesFiredCount")
    public int getRulesFiredCount() {
        return this.rulesFiredCount;
    }

    public void setRulesFiredCount(int rulesFiredCount) {
        this.rulesFiredCount = rulesFiredCount;
    }

	@JsonProperty("RulesNotFiredCount")
    public int getRulesNotFiredCount() {
        return this.rulesNotFiredCount;
    }

    public void setRulesNotFiredCount(int rulesNotFiredCount) {
        this.rulesNotFiredCount = rulesNotFiredCount;
    }

	@JsonProperty("ExecutionDuration")
    public ExecutionDuration getExecutionDuration() {
        return executionDuration;
    }

    public void setExecutionDuration(ExecutionDuration executionDuration) {
        this.executionDuration = executionDuration;
    }

    @Override
    public String toString() {
        return "DecisionServiceTrace ID=" + getID() + ", correlationID=" + getCorrelationID() + ", startedOn=" + getStartedOn() + ", completedOn" + getCompletedOn() + ", totalRuleCount=" + getTotalRuleCount() + ", totalRulesFired=" + getRulesFiredCount() + ", totalRulesNotFired=" + getRulesNotFiredCount() + ", executionDuration=" + getExecutionDuration();
    }
}