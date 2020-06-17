package com.redhat.tracelisteners;

import com.redhat.tracelisteners.messaging.AmqMessagePublisher;
import com.redhat.tracelisteners.messaging.MessagePublisher;
import com.redhat.tracelisteners.messaging.PublishingFailedException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.redhat.eventmodel.events.RuleTraceEvent;
import com.redhat.eventmodel.events.RuleTraceEventType;
import com.redhat.eventmodel.events.TraceEventType;
import com.redhat.eventmodel.model.Fact;
import com.redhat.eventmodel.model.Rule;
import org.drools.core.reteoo.InitialFactImpl;
import org.kie.api.definition.KiePackage;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.runtime.KieRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class RuleTraceEventListener implements AgendaEventListener {
	protected static final Logger LOGGER = LoggerFactory.getLogger(RuleTraceEventListener.class);
	private List<String> rulesFired = new ArrayList<>( );
	private String ruleFlowGroup = "";
	private MessagePublisher publisher = new AmqMessagePublisher();
	private String correlationKey;
	private KieRuntime runtime;

	private HashMap<String, ArrayList<String>> rulesInPackage = new HashMap<>( );

	public RuleTraceEventListener() throws Exception {
	}

	public String getCorrelationKey() {
		return correlationKey;
	}

	// call this to get the map of packages with the rules not fired.
	public HashMap<String, ArrayList<String>> getRulesNotFired() {
		return rulesInPackage;
	}
    
    public List<String> getRulesFired() {
        return this.rulesFired;
    }
    
    public void setRulesFired(List<String> listRules) {
        this.rulesFired = listRules;
    }

	//close publisher
	public void close() {
		try {
			LOGGER.debug("Closing rule event listener publisher");
			publisher.close();
		} catch (Exception e) {
			LOGGER.warn("Failed to close message", e);
		}
	}

    public void afterMatchFired(AfterMatchFiredEvent event) {
		if(rulesInPackage.size() != 0) {
			// remove the rule from the list of rules. so we can get the list of rules that were not fired.
			rulesInPackage.get(event.getMatch().getRule().getPackageName()).remove(event.getMatch().getRule().getName());
		}

//		Collection<ProcessEventListener> listenerList = event.getKieRuntime().getProcessEventListeners();
//		setupCorrelationKey();

//		if(correlationKey == null) {
//			for (ProcessEventListener listener : listenerList) {
//				if (listener instanceof ProcessTraceEventListener) {
//					LOGGER.debug("* Found correlationKey : " + ((ProcessTraceEventListener) listener).getCorrelationKey());
//					// down-casting https://www.baeldung.com/java-type-casting
//					correlationKey = ((ProcessTraceEventListener) listener).getCorrelationKey();
//					break;
//				}
//			}
//		}

		LOGGER.trace("afterMatchFired: " + event.toString());

		RuleTraceEvent ruleTraceEvent = new RuleTraceEvent();
		ruleTraceEvent.setTimeStamp(LocalDateTime.now());
		ruleTraceEvent.setEventType(TraceEventType.RuleTraceEvent);
		ruleTraceEvent.setType(RuleTraceEventType.AfterMatchFired);
		ruleTraceEvent.setID(correlationKey);
		Rule ruleModel = new Rule();
		ruleModel.setName(event.getMatch().getRule().getName());
		ruleModel.setRuleFlowGroup(ruleFlowGroup);
		ruleTraceEvent.setRule(ruleModel);
		
		ArrayList<Fact> facts = new ArrayList<>( );

		for(Object objInMem : event.getMatch().getObjects()) {
			Fact factInMem = new Fact();
			factInMem.setObject(objInMem);
			if(objInMem.getClass() == InitialFactImpl.class) { // when there is no object in the 'when' rule
				factInMem.setObject(null);
			}
			facts.add(factInMem);
		}
		
		ruleTraceEvent.setFacts(facts);

		try {
			publisher.publishMessage(ruleTraceEvent);
		} catch (PublishingFailedException e) {
			LOGGER.warn("Failed to publish message");
		}

		rulesFired.add(event.getMatch().getRule().getName());
		
    }

    private void setupCorrelationKey() {
		Collection<ProcessEventListener> listenerList = runtime.getProcessEventListeners();
		if(correlationKey == null) {
			for (ProcessEventListener listener : listenerList) {
				if (listener instanceof ProcessTraceEventListener) {
					LOGGER.debug("* Found correlationKey : " + ((ProcessTraceEventListener) listener).getCorrelationKey());
					// down-casting https://www.baeldung.com/java-type-casting
					correlationKey = ((ProcessTraceEventListener) listener).getCorrelationKey();
					break;
				}
			}
		}
	}

	public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
		runtime = event.getKieRuntime();
		setupCorrelationKey();

		// 1. Create HashMap for Packages and get all rules
		if(rulesInPackage.isEmpty()) {
			Collection<KiePackage> packages = event.getKieRuntime().getKieBase().getKiePackages();
			for(KiePackage pack: packages) {
				rulesInPackage.put(pack.getName(), new ArrayList<>( ));
				
				for(org.kie.api.definition.rule.Rule rule : pack.getRules()) {
					rulesInPackage.get(pack.getName()).add(rule.getName()); // get the key in the map. add the rules to the arraylist
				}
			}
		}
		LOGGER.debug("All rules in packages: " + rulesInPackage.toString()); // remove the rule when after match fired.

		LOGGER.trace("beforeRuleFlowGroupActivated: " + event.toString());
	}

	public void transformRulesNotFired() {
		RuleTraceEvent ruleTraceEvent = new RuleTraceEvent();
		ArrayList<Rule> rulesNotFiredList = new ArrayList<>( );
		for (Map.Entry<String, ArrayList<String>> entry : rulesInPackage.entrySet()) {
			if(entry.getValue().size() > 0) {
				for(String ruleName : entry.getValue()) {
					Rule r = new Rule();
					r.setName(ruleName);
					rulesNotFiredList.add(r);
				}
			}
		}
		ruleTraceEvent.setEventType(TraceEventType.RuleTraceEvent);
		ruleTraceEvent.setType(RuleTraceEventType.RulesNotFired);
		ruleTraceEvent.setRulesNotFired(rulesNotFiredList);
		ruleTraceEvent.setTimeStamp(LocalDateTime.now());
		ruleTraceEvent.setID(correlationKey);

		try {
			publisher.publishMessage(ruleTraceEvent);
		} catch (PublishingFailedException e) {
			LOGGER.warn("Failed to publish message", e);
		}

	}

	public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
		ruleFlowGroup = event.getRuleFlowGroup().getName();
		LOGGER.debug("RuleflowGroup Activated: " + event.getRuleFlowGroup().getName());
		LOGGER.trace("afterRuleFlowGroupActivated: " + event.toString());
	}

	public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
		LOGGER.trace("agendaGroupPopped: " + event.toString());
	}

	public void agendaGroupPushed(AgendaGroupPushedEvent event) {
		LOGGER.trace("agendaGroupPushed: " + event.toString());
	}

	public void matchCreated(MatchCreatedEvent event) {
		LOGGER.trace("MatchCreated: " + event.toString());
	}

	public void matchCancelled(MatchCancelledEvent event) {
		LOGGER.trace("MatchCancelled: " + event.toString());
	}

	public void beforeMatchFired(BeforeMatchFiredEvent event) {
		LOGGER.trace("beforeMatchFired: " + event.toString());
	}

	public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
		LOGGER.trace("beforeRuleFlowGroupDeactivated: " + event.toString());
	}

	public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
		LOGGER.trace("afterRuleFlowGroupDeactivated: " + event.toString());
		
		// send rules not fired to queue for each rule flow group
		//transformRulesNotFired();
	}
}
