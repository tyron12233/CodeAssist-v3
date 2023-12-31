package com.tyron.code.logging;

import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.event.LoggingEvent;

public class CodeAssistLoggingFilter extends Filter<LoggingEvent> {

    @Override
    public FilterReply decide(LoggingEvent event) {
        if (event.getLoggerName().startsWith("com.tyron.")) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }
}
