/**
 * Copyright 2000-2013 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.nexus.plugins.gitlab.client.rest;

import biz.neustar.nexus.plugins.gitlab.GitlabAuthenticatingRealm;
import biz.neustar.nexus.plugins.gitlab.config.v1_0_0.Configuration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserIdMatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserIdMatcher.class);
    private static final String DEFAULT_PROVIDER = "gitlab";
    private static final String DEFAULT_FIELD = "email";
    private final String userIdProvider;
    private final String userIdField;
    private final Pattern userIdPattern;

    public UserIdMatcher(Configuration config) {
        String field = config.getUserIdField();
        if (StringUtils.isBlank(field)) {
            field = DEFAULT_FIELD;
        }
        userIdField = field;


        String provider = config.getUserIdProvider();
        if (StringUtils.isBlank(provider)) {
            provider = DEFAULT_PROVIDER;
        }
        userIdProvider = provider;

        String pattern = "(.*)";
        if (StringUtils.isNotBlank(config.getUserIdFieldMatch())) {
            pattern = config.getUserIdFieldMatch();
        }
        LOGGER.debug(GitlabAuthenticatingRealm.GITLAB_MSG +
                "Using User Id Field " + userIdField + " and Pattern: " + pattern);
        userIdPattern = Pattern.compile(pattern);
    }

    public boolean matches(List<Map<String, String>> identities, String userId) {
        boolean result = false;
        LOGGER.debug(GitlabAuthenticatingRealm.GITLAB_MSG + " field: " + identities);
        if (identities != null) {
            for (Map<String, String> identity : identities) {
                String provider = identity.get("provider");
                if (provider != null && provider.equals(userIdProvider)) {
                    String field = identity.get(userIdField);
                    if (field != null) {
                        Matcher patternMatch = userIdPattern.matcher(field);
                        if (patternMatch.matches() && patternMatch.groupCount() > 0) {
                            String group = patternMatch.group(1);
                            LOGGER.debug(GitlabAuthenticatingRealm.GITLAB_MSG + " match group: " + group);
                            result = group.equals(userId);
                        }
                    }
                }
            }
        }
        return result;
    }
}
