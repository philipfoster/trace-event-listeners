package com.redhat.tracelisteners;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;


import com.redhat.eventmodel.events.DecisionModelTraceEvent;
import com.redhat.eventmodel.events.DecisionModelTraceEventType;
import com.redhat.eventmodel.events.TraceEventType;
import com.redhat.eventmodel.model.*;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.event.AfterEvaluateBKMEvent;
import org.kie.dmn.api.core.event.AfterEvaluateContextEntryEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateBKMEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateContextEntryEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMNTraceEventListener implements DMNRuntimeEventListener {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DMNTraceEventListener.class);

    private static final List<Object> decisionResults = new CopyOnWriteArrayList<>();
    private MessagePublisher publisher;
    private String correlationKey;

    public void beforeEvaluateDecision(BeforeEvaluateDecisionEvent event) {
      LOGGER.debug("BeforeEvaluateDecision: " + event.toString());
      
      if(event.getResult().getContext().get("com.gdit.rhba.correlationKey") != null) {
        correlationKey = event.getResult().getContext().get("com.gdit.rhba.correlationKey").toString();
        LOGGER.info("Got Key: " + correlationKey);
      }

    }
    
    // Got from https://github.com/kiegroup/drools/commit/5d3000e549d077c4a6ca19c66963635d71270af5
    public void afterEvaluateDecision(AfterEvaluateDecisionEvent event) {
      LOGGER.debug("AfterEvaluateDecisionEvent: " + event.toString());

      DMNDecisionResult dResult = event.getResult().getDecisionResultByName(event.getDecision().getName());
      if(dResult.getEvaluationStatus() == DMNDecisionResult.DecisionEvaluationStatus.SUCCEEDED) {
        LOGGER.debug("Decision result: " + dResult.getResult());
        
        EvaluatedDecision evaluatedDecision = new EvaluatedDecision();
        evaluatedDecision.setID(event.getDecision().getId());
        evaluatedDecision.setName(event.getDecision().getName());
        evaluatedDecision.setModelName(event.getDecision().getModelName());
        evaluatedDecision.setResultType(event.getDecision().getResultType().toString());

        List<DecisionModelExecutionResult> dmExResultList = new ArrayList<>( );
        for(DMNDecisionResult d : event.getResult().getDecisionResults()) {
          DecisionModelExecutionResult dmExResult = new DecisionModelExecutionResult();
          dmExResult.setDecisionID(d.getDecisionId());
          dmExResult.setEvaluationStatus(d.getEvaluationStatus().toString());
          dmExResult.setName(d.getDecisionName());
          dmExResult.setResult(d.getResult());
          dmExResultList.add(dmExResult);
        }
        evaluatedDecision.setResults(dmExResultList);
        DecisionModelTraceEvent dmTraceEvent = new DecisionModelTraceEvent();
        dmTraceEvent.setTimeStamp(LocalDateTime.now()); 
        dmTraceEvent.setType(DecisionModelTraceEventType.AfterEvaluateDecision);
        dmTraceEvent.setEvaluatedDecision(evaluatedDecision);
        dmTraceEvent.setEventType(TraceEventType.DecisionModelTraceEvent);
        dmTraceEvent.setID(correlationKey);

        try {
          LOGGER.debug("AfterEvaluateBKM sending to queue");
          publisher = new MessagePublisher(MessageQueueType.DMN);
          publisher.publishMessage(/*routingkey*/1, dmTraceEvent);
          
          //publisher.close();
          LOGGER.debug("Done send to queue");
        } catch (IOException | TimeoutException e) {
          e.printStackTrace();
        }

      }
    }
    
    public List<Object> getDecisionResults() {
      return java.util.Collections.unmodifiableList(decisionResults);
    }

    public void beforeEvaluateBKM(BeforeEvaluateBKMEvent event) {
      LOGGER.debug("BeforeEvaluateBKMEvent: " + event.toString());
    }
    
    public void afterEvaluateBKM(AfterEvaluateBKMEvent event) {
      LOGGER.debug("AfterEvaluateBKMEvent: " + event.toString());
      BusinessKnowledgeModel businessKnowledgeModel = new BusinessKnowledgeModel();
      //businessKnowledgeModel.setID();
      businessKnowledgeModel.setModelName(event.getBusinessKnowledgeModel().getModelName());
      businessKnowledgeModel.setName(event.getBusinessKnowledgeModel().getName());
      businessKnowledgeModel.setResultType(event.getBusinessKnowledgeModel().getResultType().getName());
      ArrayList<DecisionModelExecutionResult> dmExResultList = new ArrayList<>( );
      for(DMNDecisionResult d : event.getResult().getDecisionResults()) {
        DecisionModelExecutionResult dmExResult = new DecisionModelExecutionResult();
        dmExResult.setDecisionID(d.getDecisionId());
        dmExResult.setEvaluationStatus(d.getEvaluationStatus().toString());
        dmExResult.setName(d.getDecisionName());
        dmExResult.setResult(d.getResult());
        dmExResultList.add(dmExResult);
      }
      businessKnowledgeModel.setResults(dmExResultList);
      DecisionModelTraceEvent dmTraceEvent = new DecisionModelTraceEvent();
      dmTraceEvent.setType(DecisionModelTraceEventType.AfterEvaluateBKM);
      dmTraceEvent.setBKM(businessKnowledgeModel);
      dmTraceEvent.setEventType(TraceEventType.DecisionModelTraceEvent);
      dmTraceEvent.setTimeStamp(LocalDateTime.now());
      dmTraceEvent.setID(correlationKey);
      
      try {
        LOGGER.debug("AfterEvaluateBKM sending to queue");
        publisher = new MessagePublisher(MessageQueueType.DMN);
        publisher.publishMessage(/*routingkey*/1, dmTraceEvent);
        
        //publisher.close();
        LOGGER.debug("Done send to queue");
      } catch (IOException e) {
        e.printStackTrace();
      } catch (TimeoutException e) {
        e.printStackTrace();
      }
    }
    
    public void beforeEvaluateContextEntry(BeforeEvaluateContextEntryEvent event) {
      LOGGER.debug("BeforeEvaluateContextEntryEvent: " + event.toString());
    }
    
    public void afterEvaluateContextEntry(AfterEvaluateContextEntryEvent event) {
      LOGGER.debug("AfterEvaluateContextEntryEvent: " + event.toString());

      DecisionModelContextEntry dModelContextEntry = new DecisionModelContextEntry();
      dModelContextEntry.setNodeName(event.getNodeName());
      dModelContextEntry.setVariableName(event.getVariableName());
      dModelContextEntry.setExpressionResult(event.getExpressionResult());

      ArrayList<DecisionModelExecutionResult> dmExResultList = new ArrayList<>( );
      for(DMNDecisionResult d : event.getResult().getDecisionResults()) {
        DecisionModelExecutionResult dmExResult = new DecisionModelExecutionResult();
        dmExResult.setDecisionID(d.getDecisionId());
        dmExResult.setEvaluationStatus(d.getEvaluationStatus().toString());
        dmExResult.setName(d.getDecisionName());
        dmExResult.setResult(d.getResult());
        dmExResultList.add(dmExResult);
      }
      dModelContextEntry.setResults(dmExResultList);
      DecisionModelTraceEvent dmTraceEvent = new DecisionModelTraceEvent();
      dmTraceEvent.setType(DecisionModelTraceEventType.AfterEvaluateContextEntry);
      dmTraceEvent.setEventType(TraceEventType.DecisionModelTraceEvent);
      dmTraceEvent.setTimeStamp(LocalDateTime.now());
      dmTraceEvent.setID(correlationKey);
      
      try {
        LOGGER.debug("AfterEvaluateContextEntryEvent sending to queue");
        publisher = new MessagePublisher(MessageQueueType.DMN);
        publisher.publishMessage(/*routingkey*/1, dmTraceEvent);
        
        //publisher.close();
        LOGGER.debug("Done send to queue");
      } catch (IOException | TimeoutException e) {
        e.printStackTrace();
      }
    }
    
    public void beforeEvaluateDecisionTable(BeforeEvaluateDecisionTableEvent event) {
      LOGGER.debug("BeforeEvaluateDecisionTableEvent: " + event.toString());
    }
    
    public void afterEvaluateDecisionTable(AfterEvaluateDecisionTableEvent event) {
      LOGGER.debug("AfterEvaluateDecisionTableEvent: " + event.toString());
      DecisionTable dt = new DecisionTable();
      dt.setName(event.getDecisionTableName());
      dt.setNodeName(event.getNodeName());
      dt.setMatches(event.getMatches());
      ArrayList<DecisionModelExecutionResult> dmExResultList = new ArrayList<>( );
      for(DMNDecisionResult d : event.getResult().getDecisionResults()) {
        DecisionModelExecutionResult dmExResult = new DecisionModelExecutionResult();
        dmExResult.setDecisionID(d.getDecisionId());
        dmExResult.setEvaluationStatus(d.getEvaluationStatus().toString());
        dmExResult.setName(d.getDecisionName());
        dmExResult.setResult(d.getResult());
        dmExResultList.add(dmExResult);
      }
      dt.setResults(dmExResultList);
      
      DecisionModelTraceEvent dmTraceEvent = new DecisionModelTraceEvent();
      dmTraceEvent.setType(DecisionModelTraceEventType.AfterEvaluateDecisionTable);
      dmTraceEvent.setDecisionTable(dt);
      dmTraceEvent.setEventType(TraceEventType.DecisionModelTraceEvent);
      dmTraceEvent.setTimeStamp(LocalDateTime.now());
      dmTraceEvent.setID(correlationKey);
      
      try {
        LOGGER.debug("AfterEvaluateDecisionTableEvent sending to queue");
        publisher = new MessagePublisher(MessageQueueType.DMN);
        publisher.publishMessage(/*routingkey*/1, dmTraceEvent);
        
        //publisher.close();
        LOGGER.debug("Done send to queue");
      } catch (IOException | TimeoutException e) {
        e.printStackTrace();
      }
    }
    
    public void beforeEvaluateDecisionService(BeforeEvaluateDecisionServiceEvent event) {
      LOGGER.debug("BeforeEvaluateDecisionServiceEvent: " + event.toString());
    }
    
    public void afterEvaluateDecisionService(AfterEvaluateDecisionServiceEvent event) {
      LOGGER.debug("AfterEvaluateDecisionServiceEvent: " + event.toString());
    }
}
