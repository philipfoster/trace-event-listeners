package com.redhat.tracelisteners.messaging;

import com.redhat.eventmodel.events.ProcessTraceEvent;
import com.redhat.eventmodel.events.RuleTraceEvent;
import com.redhat.eventmodel.events.WorkingMemoryTraceEvent;
import javax.naming.NamingException;

public interface MessagePublisher extends AutoCloseable {

    void publishMessage(RuleTraceEvent event) throws PublishingFailedException;
    void publishMessage(WorkingMemoryTraceEvent event) throws PublishingFailedException;
    void publishMessage(ProcessTraceEvent event) throws PublishingFailedException;

}
