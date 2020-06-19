package com.redhat.tracelisteners.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Objects;

@JsonPropertyOrder({"Name", "RuleFlowGroup"})
public class Rule {

	private String name;
	private String ruleFlowGroup;

	@JsonProperty("Name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("RuleFlowGroup")
	public String getRuleFlowGroup() {
		return this.ruleFlowGroup;
	}
	
	public void setRuleFlowGroup(String ruleFlowGroup) {
		this.ruleFlowGroup = ruleFlowGroup;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Rule rule = (Rule) o;
		return Objects.equals( getName(), rule.getName() ) &&
				Objects.equals( getRuleFlowGroup(), rule.getRuleFlowGroup());
	}

	@Override
	public int hashCode() {
		return Objects.hash( getName(), getRuleFlowGroup() );
	}

    @Override
    public String toString() {
        return "Rule name=" + getName() + ", ruleFlowGroup=" + getRuleFlowGroup();
    }
}
