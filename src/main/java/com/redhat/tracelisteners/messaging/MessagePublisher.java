package com.redhat.tracelisteners.messaging;

import com.redhat.eventmodel.events.ProcessTraceEvent;
import com.redhat.eventmodel.events.RuleTraceEvent;
import com.redhat.eventmodel.events.WorkingMemoryTraceEvent;

public interface MessagePublisher {

    void publishMessage(RuleTraceEvent event) throws PublishingFailedException;
    void publishMessage(WorkingMemoryTraceEvent event) throws PublishingFailedException;
    void publishMessage(ProcessTraceEvent event) throws PublishingFailedException;

}
