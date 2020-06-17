package com.redhat.tracelisteners.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.eventmodel.events.ProcessTraceEvent;
import com.redhat.eventmodel.events.RuleTraceEvent;
import com.redhat.eventmodel.events.WorkingMemoryTraceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import static com.redhat.tracelisteners.conf.ConfigManager.getJndiPassword;
import static com.redhat.tracelisteners.conf.ConfigManager.getJndiUser;

public class AmqMessagePublisher implements MessagePublisher, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmqMessagePublisher.class);
    private static final String JNDI_CONNECTION_FACTORY = "myFactoryLookup";
    private static final String JNDI_PROCESS_EVENTS_QUEUE = "process-queue";
    private static final String JNDI_RULE_EVENTS_QUEUE = "rule-queue";
    private static final String JNDI_WORKING_MEMORY_QUEUE = "working-memory-queue";
    private static final int DELIVERY_MODE = DeliveryMode.NON_PERSISTENT;

    private final Context context;
    private final Connection connection;
    private final ObjectMapper mapper;

    public AmqMessagePublisher() throws Exception {
        // TODO: Find better way to  create the connection.
        // I think that PAM will create a new instance of our publisher for each process instance. If this is the case, this is a
        // very inefficient use of connections, and will likely cause performance issues when running under load.
        context = new InitialContext();
        mapper = new ObjectMapper();

        ConnectionFactory factory = (ConnectionFactory) context.lookup(JNDI_CONNECTION_FACTORY);

        connection = factory.createConnection(getJndiUser(), getJndiPassword());
        connection.setExceptionListener(new AmqExceptionListener());
        connection.start();
    }

    @Override
    public void publishMessage(ProcessTraceEvent event) throws PublishingFailedException {
        LOGGER.debug("Publishing process trace event");
        publishMessage(JNDI_PROCESS_EVENTS_QUEUE, event);
    }


    @Override
    public void publishMessage(RuleTraceEvent event) throws PublishingFailedException {
        LOGGER.debug("Publishing rule trace event");
        publishMessage(JNDI_RULE_EVENTS_QUEUE, event);
    }

    @Override
    public void publishMessage(WorkingMemoryTraceEvent event) throws PublishingFailedException {
        LOGGER.debug("Publishing working memory trace event");
        publishMessage(JNDI_WORKING_MEMORY_QUEUE, event);
    }

    private void publishMessage(String destName, Object event) throws PublishingFailedException {
        try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            Destination dest = (Destination) context.lookup(destName);
            MessageProducer producer = session.createProducer(dest);

            String eventStr = mapper.writeValueAsString(event);

            TextMessage message = session.createTextMessage(eventStr);
            producer.send(message, DELIVERY_MODE, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        } catch (Exception e) {
            LOGGER.error("Failed to publish message to queue " + destName, e);
            throw new PublishingFailedException(e);
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
