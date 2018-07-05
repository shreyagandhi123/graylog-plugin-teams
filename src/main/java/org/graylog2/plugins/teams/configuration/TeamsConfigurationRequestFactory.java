package org.graylog2.plugins.teams.configuration;

import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.configuration.fields.TextField;

public class TeamsConfigurationRequestFactory {

    public static ConfigurationRequest createTeamsMessageOutputConfigurationRequest() {
        final ConfigurationRequest configurationRequest = new ConfigurationRequest();
        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_COLOR, "Custom Message & Additional Info Color", "#FF0000",
                "Color to use for Teams custom message and additional information attachments",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );
        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_TEXT, "text",
                "##########\n" +
                        "Date: ${check_result.triggeredAt}\n" +
                        "Stream ID: ${stream.id}\n" +
                        "Stream title: ${stream.title}\n" +
                        "Stream description: ${stream.description}\n" +
                        "${if stream_url}Stream URL: ${stream_url}${end}\n" +
                        "##########\n",
                "Custom message to be appended below the alert title. " +
                        "The following properties are available for template building: " +
                        "\"stream\", " +
                        "\"message\", " +
                        " \"stream_url\"," +
                        "See http://docs.graylog.org/en/2.3/pages/streams/alerts.html#email-alert-notification for more details.",
                ConfigurationField.Optional.OPTIONAL,
                TextField.Attribute.TEXTAREA)
        );

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_WEBHOOK_URL, "Webhook URL", "", "Teams \"Incoming Webhook\" URL",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_PROXY_ADDRESS, "Proxy", null,
                "Please insert the proxy information in the follwoing format: <ProxyAddress>:<Port>",
                ConfigurationField.Optional.OPTIONAL)
        );

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_SUMMARY, "Summary", "New Alert",
                "Summary of Post",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_TITLE, "Title", "Graylog ",
                "Title of Post",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_CONTEXT, "@context", "https://schema.org/extensions", "",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );
      
        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_TYPE, "@type", "MessageCard", "",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );

        return configurationRequest;
    }

    public static ConfigurationRequest createTeamsAlarmCallbackConfigurationRequest() {
        final ConfigurationRequest configurationRequest = new ConfigurationRequest();

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_COLOR, "Custom Message & Additional Info Color", "#FF0000",
                "Color to use for Teams custom message and additional information attachments",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );
        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_TEXT, "text",
                "##########\n" +
                        "Date: ${check_result.triggeredAt}\n" +
                        "Stream ID: ${stream.id}\n" +
                        "Stream title: ${stream.title}\n" +
                        "Stream description: ${stream.description}\n" +
                        "${if stream_url}Stream URL: ${stream_url}${end}\n" +
                        "##########\n",
                "Custom message to be appended below the alert title. " +
                        "The following properties are available for template building: " +
                        "\"stream\", " +
                        "\"message\", " +
                        " \"stream_url\"," +
                        "See http://docs.graylog.org/en/2.3/pages/streams/alerts.html#email-alert-notification for more details.",
                ConfigurationField.Optional.OPTIONAL,
                TextField.Attribute.TEXTAREA)
        );

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_WEBHOOK_URL, "Webhook URL", "", "Teams \"Incoming Webhook\" URL",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_CONTEXT, "@context", "https://schema.org/extensions", "",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );
      
        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_TYPE, "@type", "MessageCard", "",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_SUMMARY, "Summary", "New Alert", "",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_TITLE, "Title", "Greylog", "",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );

        configurationRequest.addField(new TextField(
                TeamsConfiguration.CK_PROXY_ADDRESS, "Proxy", null,
                "Please insert the proxy information in the follwoing format: <ProxyAddress>:<Port>",
                ConfigurationField.Optional.OPTIONAL)
        );

        return configurationRequest;
    }

}