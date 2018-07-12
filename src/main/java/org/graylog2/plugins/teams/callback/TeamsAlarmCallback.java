package org.graylog2.plugins.teams.callback;

import com.floreysoft.jmte.Engine;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugins.teams.TeamsClient;
import org.graylog2.plugins.teams.TeamsMessage;
import org.graylog2.plugins.teams.TeamsPluginBase;
import org.graylog2.plugins.teams.configuration.TeamsConfiguration;
import org.graylog2.plugins.teams.configuration.TeamsConfigurationRequestFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

public class TeamsAlarmCallback extends TeamsPluginBase implements AlarmCallback {

    private final Engine templateEngine;

    @Inject
    public TeamsAlarmCallback(Engine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public void initialize(final Configuration config) throws AlarmCallbackConfigurationException {
        try {
            super.setConfiguration(config);
        } catch (ConfigurationException e) {
            throw new AlarmCallbackConfigurationException("Configuration error. " + e.getMessage());
        }
    }

    @Override
    public void call(Stream stream, AlertCondition.CheckResult result) {
        final TeamsClient client = new TeamsClient(configuration);
        String ttext = buildFullMessageBody(stream, result);

        ArrayList section = new ArrayList();
        Map<String, Object> section1 = new HashMap<String, Object>();
        ArrayList facts = new ArrayList();
        Map<String, Object> mMap = new HashMap<String, Object>();

        String temp = result.getTriggeredAt().toString();
        mMap.put("name", "Date: ");
        mMap.put("value",temp);
        facts.add(mMap);
        temp = stream.getTitle();
        mMap = new HashMap<String, Object>();
        mMap.put("name", "Stream Title: ");
        mMap.put("value",temp);
        facts.add(mMap);
        temp = stream.getDescription();
        mMap = new HashMap<String, Object>();
        mMap.put("name", "Stream Description: ");
        mMap.put("value",temp);
        facts.add(mMap);
        temp = result.getResultDescription();
        mMap = new HashMap<String, Object>();
        mMap.put("name", "Result Description: ");
        mMap.put("value",temp);
        facts.add(mMap);
        temp = result.getTriggeredCondition().toString();
        mMap = new HashMap<String, Object>();
        mMap.put("name", "Triggered Condition: ");
        mMap.put("value",temp);
        facts.add(mMap);

        section1.put("facts", facts);
        section1.put("text", ttext);

        section.add(section1);

        TeamsMessage teamsMessage = createTeamsMessage(configuration, section);

        try {
            client.send(teamsMessage);
        } catch (TeamsClient.TeamsClientException e) {
            throw new RuntimeException("Could not send message to Teams.", e);
        }
    }

    private String buildFullMessageBody(Stream stream, AlertCondition.CheckResult result) {
        String graylogUri = "";
        String titleLink;
        if (!isNullOrEmpty(graylogUri)) {
            titleLink = "<" + buildStreamLink(graylogUri, stream) + "|" + stream.getTitle() + ">";
        } else {
            titleLink = "*" + stream.getTitle() + "*";
        }

        // Build custom message
        String description = result.getResultDescription();
        return String.format("**Alert for Graylog stream %s**:\n> %s \n", titleLink, description);
    }

    private String buildCustomMessage(Stream stream, AlertCondition.CheckResult result, String template) {
        List<Message> backlog = getAlarmBacklog(result);
        Map<String, Object> model = getModel(stream, result, backlog);
        try {
            return templateEngine.transform(template, model);
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private List<Message> getAlarmBacklog(AlertCondition.CheckResult result) {
        final AlertCondition alertCondition = result.getTriggeredCondition();
        final List<MessageSummary> matchingMessages = result.getMatchingMessages();
        final int effectiveBacklogSize = Math.min(alertCondition.getBacklog(), matchingMessages.size());

        if (effectiveBacklogSize == 0) return Collections.emptyList();
        final List<MessageSummary> backlogSummaries = matchingMessages.subList(0, effectiveBacklogSize);
        final List<Message> backlog = Lists.newArrayListWithCapacity(effectiveBacklogSize);
        for (MessageSummary messageSummary : backlogSummaries) {
            backlog.add(messageSummary.getRawMessage());
        }

        return backlog;
    }

    private Map<String, Object> getModel(Stream stream, AlertCondition.CheckResult result, List<Message> backlog) {
        Map<String, Object> model = new HashMap<>();
        String graylogUri = "";
        model.put("stream", stream);
        model.put("check_result", result);
        model.put("alert_condition", result.getTriggeredCondition());
        model.put("backlog", backlog);
        model.put("backlog_size", backlog.size());
        if (!isNullOrEmpty(graylogUri)) {
            model.put("stream_url", buildStreamLink(graylogUri, stream));
        }

        return model;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return configuration.getSource();
    }

    @Override
    public void checkConfiguration() {
        /* Never actually called by graylog-server */
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {
        return TeamsConfigurationRequestFactory.createTeamsAlarmCallbackConfigurationRequest();
    }

    @Override
    public String getName() {
        return "Teams Alarm Callback";
    }
}
