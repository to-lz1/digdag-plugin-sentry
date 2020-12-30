package io.digdag.plugin.sentry;

import io.digdag.spi.OperatorFactory;
import io.digdag.spi.OperatorProvider;
import io.digdag.spi.Plugin;

import java.util.Collections;
import java.util.List;


public class SentryPlugin implements Plugin {
    @Override
    public <T> Class<? extends T> getServiceProvider(Class<T> type) {
        if (type == OperatorProvider.class) {
            return SentryOperatorProvider.class.asSubclass(type);
        } else {
            return null;
        }
    }

    public static class SentryOperatorProvider implements OperatorProvider {
        @Override
        public List<OperatorFactory> get() {
            return Collections.singletonList(new SentryOperatorFactory());
        }
    }
}
