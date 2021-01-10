package io.digdag.plugin.sentry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DigdagErrorPayload {

    public final String message;

    public final String stacktrace;

    @JsonCreator
    public DigdagErrorPayload(@JsonProperty("message") String message,
                              @JsonProperty("stacktrace") String stacktrace) {
        this.message = message;
        this.stacktrace = stacktrace;
    }

    @Override
    public String toString() {
        return "message='" + message + '\'' + '\n' + "stacktrace='" + stacktrace.replace(", ", "\n") + '\'';
    }

    public RuntimeException asException() {
        return new DigdagSentryPluginException(this.toString());
    }

    private static class DigdagSentryPluginException extends RuntimeException {
        public DigdagSentryPluginException(String message) {
            super(message);
        }
    }
}
