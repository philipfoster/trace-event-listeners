package com.redhat.tracelisteners.messaging;

import com.redhat.tracelisteners.events.ProcessTraceEvent;

public interface MessagePublisher {

    //    void publishMessage(RuleTraceEvent event) throws PublishingFailedException;
//    void publishMessage(WorkingMemoryTraceEvent event) throws PublishingFailedException;
    void publishMessage(ProcessTraceEvent event) throws PublishingFailedException;

}
