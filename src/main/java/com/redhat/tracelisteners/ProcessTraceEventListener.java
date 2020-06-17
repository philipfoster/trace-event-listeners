package com.redhat.tracelisteners;


import com.redhat.eventmodel.events.ProcessTraceEvent;
import com.redhat.eventmodel.events.ProcessTraceEventType;
import com.redhat.eventmodel.events.TraceEventType;
import com.redhat.eventmodel.model.Node;
import com.redhat.eventmodel.model.NodeState;
import com.redhat.eventmodel.model.NodeType;
import com.redhat.eventmodel.model.Process;
import com.redhat.tracelisteners.messaging.AmqMessagePublisher;
import com.redhat.tracelisteners.messaging.MessagePublisher;
import com.redhat.tracelisteners.messaging.PublishingFailedException;
import java.time.LocalDateTime;
import java.util.Collection;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessTraceEventListener implements ProcessEventListener {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessTraceEventListener.class);
    private MessagePublisher publisher = new AmqMessagePublisher();
    private LocalDateTime nodeStartTime;

    public ProcessTraceEventListener() throws Exception {
    }


    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        LOGGER.trace("BeforeNodeTriggered: " + event.toString());
        nodeStartTime = LocalDateTime.now();
    }

    public void beforeProcessStarted(ProcessStartedEvent event) {
        LOGGER.trace("BeforeProcessStarted: " + event.toString());

        RuleFlowProcessInstance rfpi = (RuleFlowProcessInstance) event.getProcessInstance();

        String id = Long.toString(rfpi.getId());

        ProcessTraceEvent processTraceEvent = new ProcessTraceEvent();
        processTraceEvent.setEventType(TraceEventType.ProcessTraceEvent);
        processTraceEvent.setTimeStamp(LocalDateTime.now());
        processTraceEvent.setType(ProcessTraceEventType.BeforeProcessStarted);
        processTraceEvent.setID(id);
        com.redhat.eventmodel.model.Process process = new Process();
        process.setName(event.getProcessInstance().getProcessName());

        // if parent process instance id is -1, then it is the top most process
        if(event.getProcessInstance().getParentProcessInstanceId() != -1) {
            process.setParentProcessID(event.getProcessInstance().getParentProcessInstanceId());
        }
        processTraceEvent.setProcess(process);

        try {
            LOGGER.debug("BeforeProcessStarted sending to queue");
//            publisher = new MessagePublisher(MessageQueueType.PROCESS);
            publisher.publishMessage(processTraceEvent);
        } catch (PublishingFailedException e) {
            e.printStackTrace();
        }
    }

    public void afterProcessCompleted(ProcessCompletedEvent event) {
        LOGGER.trace("AfterProcessCompleted: " + event.toString());
        if(event.getProcessInstance().getParentProcessInstanceId() == -1 ) {
            sendProcessCompletedEvent(event);

            // close the publisher when everything is done
            try {
                LOGGER.debug("Closing process listener publisher");
                publisher.close();
            } catch (Exception e) {
                LOGGER.warn("Failed to close publisher", e);
            }
        }
    }



    private void sendProcessCompletedEvent(ProcessCompletedEvent event) {
        RuleFlowProcessInstance rfpi = (RuleFlowProcessInstance) event.getProcessInstance();

        String id = Long.toString(rfpi.getId());

        // Send process completed event
        ProcessTraceEvent processTraceEvent = new ProcessTraceEvent();
        processTraceEvent.setEventType(TraceEventType.ProcessTraceEvent);
        processTraceEvent.setTimeStamp(LocalDateTime.now());
        processTraceEvent.setType(ProcessTraceEventType.AfterProcessCompleted);
        processTraceEvent.setID(id);
        com.redhat.eventmodel.model.Process process = new Process();
        process.setName(event.getProcessInstance().getProcessName());

        // if parent process instance id is -1, then it is the top most process
        if(event.getProcessInstance().getParentProcessInstanceId() != -1) {
            process.setParentProcessID(event.getProcessInstance().getParentProcessInstanceId());
        }
        processTraceEvent.setProcess(process);

        // if parent process instance id is -1, then it is the top most process
        if(event.getProcessInstance().getParentProcessInstanceId() != -1) {
            process.setParentProcessID(event.getProcessInstance().getParentProcessInstanceId());
        }
        processTraceEvent.setProcess(process);

        try {
            LOGGER.debug("BeforeProcessStarted sending to queue");
            publisher.publishMessage(processTraceEvent);
        } catch (PublishingFailedException e) {
            LOGGER.warn("Failed to publish message", e);
//            e.printStackTrace();
        }

    }

    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        LOGGER.trace("BeforeProcessCompleted: " + event.toString());
    }

    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        LOGGER.trace("AfterNodeLeft: " + event.toString());
    }

    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        LOGGER.trace("AfterNodeTriggered: " + event.toString());

        String id = Long.toString(event.getProcessInstance().getId());

        ProcessTraceEvent processTraceEvent = new ProcessTraceEvent();
        processTraceEvent.setEventType(TraceEventType.ProcessTraceEvent);
        processTraceEvent.setTimeStamp(LocalDateTime.now());
        processTraceEvent.setType(ProcessTraceEventType.NodeTriggered);
        processTraceEvent.setID(id);

        Node node = new Node();
        node.setType(NodeType.ProcessService);
        if(event.getNodeInstance().getNode().getClass() == org.jbpm.workflow.core.node.RuleSetNode.class) {
           node.setType(NodeType.DecisionService);
        }
        node.setState(NodeState.Completed);
        node.setStartedOn(nodeStartTime);
        node.setCompletedOn(LocalDateTime.now());
        node.setID(Long.toString(event.getNodeInstance().getId()));
        node.setName(event.getNodeInstance().getNodeName());
        
        Process process = new Process();
        process.setNode(node);
        process.setName(event.getProcessInstance().getProcessName());
        if(event.getProcessInstance().getParentProcessInstanceId() != -1) {
            process.setParentProcessID(event.getProcessInstance().getParentProcessInstanceId());
        }
        
        processTraceEvent.setProcess(process);

        //            publisher = new MessagePublisher(MessageQueueType.PROCESS);
        try {
            publisher.publishMessage(processTraceEvent);
        } catch (PublishingFailedException e) {
            LOGGER.warn("Failed to publish message", e);
//            e.printStackTrace();
        }
    }

    public void afterProcessStarted(ProcessStartedEvent event) {
        LOGGER.trace("AfterProcessStarted: " + event.toString());
    }

    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        LOGGER.trace("AfterVariableChanged: " + event.toString());
    }

    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        LOGGER.trace("BeforeNodeLeft: " + event.toString());
    }

    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        LOGGER.trace("BeforeVariableChanged: " + event.toString());
    }

}