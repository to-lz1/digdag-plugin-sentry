package io.digdag.plugin.sentry;

import io.digdag.client.config.Config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SentryTagsResolver {

    private Config config;

    public SentryTagsResolver(Config config) {
        this.config = config;
    }

    private static List<String> coreKeys = Arrays.asList(
            "timezone",
            "session_uuid",
            "session_time",
            "session_id",
            "project_id",
            "attempt_id",
            "task_name"
    );

    public Map<String, String> asMap() {
        Map<String, String> map = new java.util.HashMap<>();
        coreKeys.forEach(key -> map.put(key, config.get(key, String.class)));

        Config sentryConfig = config.getNestedOrGetEmpty("sentry_tags");
        sentryConfig.getKeys().forEach(key -> map.put(key, sentryConfig.get(key, String.class)));

        return map;
    }
}
