package com.redhat.tracelisteners;

import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkingMemoryListener implements RuleRuntimeEventListener {
    protected static final Logger LOGGER = LoggerFactory.getLogger(WorkingMemoryListener.class);

    public void objectInserted(ObjectInsertedEvent event){
        LOGGER.info("--> Fact inserted: type=" + event.getObject().getClass() + ", fact=" + event.getObject());
    }

    public void objectUpdated(ObjectUpdatedEvent event) {
        LOGGER.info("--> Fact updated by rule=" + event.getRule().getName());
        LOGGER.info("--> Fact updated: originalFact=" + event.getOldObject() + ", updatedFact=" + event.getObject());
    }

    public void objectDeleted(ObjectDeletedEvent event) {
        LOGGER.info("--> Fact deleted: fact=" + event.getOldObject());
    }
}



















