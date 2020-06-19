package com.redhat.tracelisteners.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.tracelisteners.events.ProcessTraceEvent;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmqMessagePublisher implements MessagePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmqMessagePublisher.class);
    private static final String JNDI_CONNECTION_FACTORY = "java:/RemoteJmsXA";
    private static final String JNDI_PROCESS_EVENTS_QUEUE = "java:/queue/testQueue";
    private static final String JNDI_RULE_EVENTS_QUEUE = "rule-queue";
    private static final String JNDI_WORKING_MEMORY_QUEUE = "working-memory-queue";

    private InitialContext context;
    private ConnectionFactory connectionFactory;
    private final ObjectMapper mapper;

    public AmqMessagePublisher() throws Exception {
        // TODO: Find better way to  create the connection.
        // I think that PAM will create a new instance of our publisher for each process instance. If this is the case, this is a
        // very inefficient use of connections, and will likely cause performance issues when running under load.
        mapper = new ObjectMapper();

        LOGGER.info("Initializing JMS connection factory");
        context = new InitialContext();
        connectionFactory = (ConnectionFactory) context.lookup(JNDI_CONNECTION_FACTORY);

        LOGGER.info("Successfully loaded JMS connection factory from JNDI");
    }

    @Override
    public void publishMessage(ProcessTraceEvent event) throws PublishingFailedException {
        LOGGER.debug("Publishing process trace event");
        publishMessage(JNDI_PROCESS_EVENTS_QUEUE, event);
    }

//
//    @Override
//    public void publishMessage(RuleTraceEvent event) throws PublishingFailedException {
//        LOGGER.debug("Publishing rule trace event");
//        publishMessage(JNDI_RULE_EVENTS_QUEUE, event);
//    }
//
//    @Override
//    public void publishMessage(WorkingMemoryTraceEvent event) throws PublishingFailedException {
//        LOGGER.debug("Publishing working memory trace event");
//        publishMessage(JNDI_WORKING_MEMORY_QUEUE, event);
//    }

    private void publishMessage(String destName, Object event) throws PublishingFailedException {
        try {
            String eventStr = mapper.writeValueAsString(event);
            System.out.println("DEST " + destName + " event = " + eventStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try (Connection connection = connectionFactory.createConnection()) {
            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)){
                Destination dest = (Destination) context.lookup(destName);
                MessageProducer producer = session.createProducer(dest);

                String eventStr = mapper.writeValueAsString(event);

                TextMessage message = session.createTextMessage(eventStr);
                producer.send(message);
            } catch(Exception e){
                LOGGER.error("Failed to publish message to queue " + destName, e);
                throw new PublishingFailedException(e);
            }
        } catch (JMSException e) {
            LOGGER.error("Failed to obtain JMS connection", e);
            throw new PublishingFailedException(e);
        }
    }
}
