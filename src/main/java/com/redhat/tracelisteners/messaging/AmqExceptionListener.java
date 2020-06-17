package com.redhat.tracelisteners.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;


public class AmqExceptionListener implements ExceptionListener {

    private static final Logger logger = LoggerFactory.getLogger(AmqExceptionListener.class);


    @Override
    public void onException(JMSException e) {
        logger.warn("Got exception when publishing message", e);
    }
}
