package org.graylog2.plugins.teams;

import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugins.teams.configuration.TeamsConfiguration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;

public class TeamsPluginBase {

    protected Configuration configuration;

    public void setConfiguration(final Configuration config) throws ConfigurationException {
        this.configuration = config;

        try {
            checkConfiguration(config);
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Configuration error. " + e.getMessage());
        }
    }

    protected void checkConfiguration(Configuration configuration) throws ConfigurationException {
        if (!configuration.stringIsSet(TeamsConfiguration.CK_WEBHOOK_URL)) {
            throw new ConfigurationException(TeamsConfiguration.CK_WEBHOOK_URL + " is mandatory and must not be empty.");
        }

        if (!configuration.stringIsSet(TeamsConfiguration.CK_CONTEXT)) {
            throw new ConfigurationException(TeamsConfiguration.CK_CONTEXT + " is mandatory and must not be empty.");
        }

        if (!configuration.stringIsSet(TeamsConfiguration.CK_TYPE)) {
            throw new ConfigurationException(TeamsConfiguration.CK_TYPE + " is mandatory and must not be empty.");
        }

        if (!configuration.stringIsSet(TeamsConfiguration.CK_TITLE)) {
            throw new ConfigurationException(TeamsConfiguration.CK_TITLE + " is mandatory and must not be empty.");
        }

        if (!configuration.stringIsSet(TeamsConfiguration.CK_SUMMARY)) {
            throw new ConfigurationException(TeamsConfiguration.CK_SUMMARY + " is mandatory and must not be empty.");
        }

        checkUri(configuration, TeamsConfiguration.CK_PROXY_ADDRESS);
    }

    private static void checkUri(Configuration configuration, String settingName) throws ConfigurationException {
        if (configuration.stringIsSet(settingName)) {
            try {
                final URI uri = new URI(Objects.requireNonNull(configuration.getString(settingName)));
                if (!isValidUriScheme(uri, "http", "https")) {
                    throw new ConfigurationException(settingName + " must be a valid HTTP or HTTPS URL.");
                }
            } catch (URISyntaxException e) {
                throw new ConfigurationException("Couldn't parse " + settingName + " correctly.", e);
            }
        }
    }

    private static boolean isValidUriScheme(URI uri, String... validSchemes) {
        return uri.getScheme() != null && Arrays.binarySearch(validSchemes, uri.getScheme(), null) >= 0;
    }

    protected String buildStreamLink(String baseUrl, Stream stream) {
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        return baseUrl + "streams/" + stream.getId() + "/messages?q=%2A&rangetype=relative&relative=3600";
    }

    protected static TeamsMessage createTeamsMessage(Configuration configuration, String message) {
        String color = configuration.getString(TeamsConfiguration.CK_COLOR);
        String type = configuration.getString(TeamsConfiguration.CK_TYPE);
        String context = configuration.getString(TeamsConfiguration.CK_CONTEXT);
        String summary = configuration.getString(TeamsConfiguration.CK_SUMMARY);
        String title = configuration.getString(TeamsConfiguration.CK_TITLE);

        return new TeamsMessage(color, context,type, summary,title, message);
    }

    protected String buildMessageLink(String baseUrl, String index, String id) {
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        return baseUrl + "messages/" + index + "/" + id;
    }
}
