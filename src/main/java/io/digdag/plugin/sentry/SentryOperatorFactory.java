package io.digdag.plugin.sentry;

import com.google.common.base.Optional;
import io.digdag.client.config.Config;
import io.digdag.client.config.ConfigException;
import io.digdag.spi.*;
import io.digdag.util.BaseOperator;
import io.sentry.Sentry;
import io.sentry.SentryLevel;

import java.util.Arrays;
import java.util.Collections;


public class SentryOperatorFactory implements OperatorFactory {

    public SentryOperatorFactory() {
    }

    public String getType() {
        return "sentry";
    }

    @Override
    public Operator newOperator(OperatorContext context) {
        return new SentryOperator(context);
    }

    private static class SentryOperator extends BaseOperator {
        public SentryOperator(OperatorContext context) {
            super(context);
        }

        @Override
        public TaskResult runTask() {
            Config params = request.getConfig();
            SecretProvider secrets = context.getSecrets().getSecrets("sentry");

            this.initSentry(params, secrets);
            SentryTagsResolver tagsResolver = new SentryTagsResolver(params);
            Sentry.configureScope(scope -> tagsResolver.asMap().forEach(scope::setTag));

            String level = params.getOptional("_command", String.class).or("error");
            Optional<String> message = params.getOptional("message", String.class);
            Optional<DigdagErrorPayload> errorPayload = params.getOptional("error", DigdagErrorPayload.class);

            if (message.isPresent()) {
                Sentry.captureMessage(message.get(), this.commandToSentryLevel(level));
            } else if (errorPayload.isPresent()) {
                Sentry.captureException(errorPayload.get().asException());
            } else {
                throw new ConfigException("Neither 'message' nor 'error' param is not found. " +
                        "Use this plugin in _error: context, or specify a message for Sentry to capture.");
            }
            return TaskResult.empty(request);
        }

        private void initSentry(Config params, SecretProvider secrets) {
            Optional<String> dsn = params.getOptional("dsn", String.class)
                    .or(secrets.getSecretOptional("dsn"));
            if (!dsn.isPresent()) {
                throw new ConfigException("Sentry dsn is not set. please set it in .dig file configuration or secrets.");
            } else {
                Sentry.init(options -> {
                    options.setDsn(dsn.get());
                    options.setBeforeSend(((event, hint) -> {
                        if(!event.isErrored()) {
                            // set message as fingerprint (because stacktrace from this plugin will always be the same)
                            // see also: https://docs.sentry.io/product/sentry-basics/guides/grouping-and-fingerprints/
                            event.setFingerprints(Collections.singletonList(event.getMessage().getFormatted()));
                        }
                        return event;
                    }));
                });
            }
        }

        private SentryLevel commandToSentryLevel(String command) {
            switch (command) {
                case "fatal":
                    return SentryLevel.FATAL;
                case "warning":
                    return SentryLevel.WARNING;
                case "info":
                    return SentryLevel.INFO;
                case "debug":
                    return SentryLevel.DEBUG;
                case "error":
                default:
                    return SentryLevel.ERROR;
            }
        }
    }
}
