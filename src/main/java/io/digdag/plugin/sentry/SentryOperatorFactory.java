package io.digdag.plugin.sentry;

import com.google.common.base.Optional;
import io.digdag.client.config.Config;
import io.digdag.client.config.ConfigException;
import io.digdag.spi.*;
import io.digdag.util.BaseOperator;
import io.sentry.Sentry;


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

            Optional<DigdagErrorPayload> errorPayload = params.getOptional("error", DigdagErrorPayload.class);
            if (!errorPayload.isPresent()) {
                throw new ConfigException("Parameter 'error' is not found. Using sentry operator outside _error: directive is not supported.");
            }

            initSentry(params, secrets);
            SentryTagsResolver tagsResolver = new SentryTagsResolver(params);
            Sentry.configureScope(scope -> tagsResolver.asMap().forEach(scope::setTag));

            Sentry.captureException(errorPayload.get().asException());
            return TaskResult.empty(request);
        }

        private void initSentry(Config params, SecretProvider secrets) {
            Optional<String> dsn = params.getOptional("dsn", String.class)
                    .or(secrets.getSecretOptional("dsn"));
            if (!dsn.isPresent()) {
                throw new ConfigException("Sentry dsn is not set. please set it in .dig file configuration or secrets.");
            } else {
                Sentry.init(options -> options.setDsn(dsn.get()));
            }
        }
    }
}
