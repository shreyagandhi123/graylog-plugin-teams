package org.graylog2.plugins.teams.output;

import com.floreysoft.jmte.Engine;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.outputs.MessageOutputConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugins.teams.TeamsClient;
import org.graylog2.plugins.teams.TeamsMessage;
import org.graylog2.plugins.teams.TeamsPluginBase;
import org.graylog2.plugins.teams.configuration.TeamsConfiguration;
import org.graylog2.plugins.teams.configuration.TeamsConfigurationRequestFactory;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Strings.isNullOrEmpty;

public class TeamsMessageOutput extends TeamsPluginBase implements MessageOutput {
    private final Engine templateEngine;
    private AtomicBoolean running = new AtomicBoolean(false);

    private final Configuration configuration;
    private final Stream stream;

    private final TeamsClient client;

    @Inject
    public TeamsMessageOutput(
            @Assisted Stream stream,
            @Assisted Configuration configuration,
            Engine templateEngine
    ) throws MessageOutputConfigurationException {
        this.configuration = configuration;
        this.stream = stream;
        this.templateEngine = templateEngine;

        // Check configuration.
        try {
            checkConfiguration(configuration);
        } catch (ConfigurationException e) {
            throw new MessageOutputConfigurationException("Missing configuration: " + e.getMessage());
        }

        this.client = new TeamsClient(configuration);

        running.set(true);
    }

    @Override
    public void stop() {
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void write(Message msg) throws RuntimeException {
        boolean shortMode = configuration.getBoolean(TeamsConfiguration.CK_SHORT_MODE);
        String message = shortMode ? buildShortMessageBody(msg) : buildFullMessageBody(stream, msg);
        TeamsMessage teamsMessage = createTeamsMessage(configuration, message);

        // Add custom message
        String template = configuration.getString(TeamsConfiguration.CK_TEXT);
        Boolean hasTemplate = !isNullOrEmpty(template);
        if (!shortMode && hasTemplate) {
            String customMessage = buildCustomMessage(stream, msg, template);
            teamsMessage.setCustomMessage(customMessage);
        }

        try {
            client.send(teamsMessage);
        } catch (TeamsClient.TeamsClientException e) {
            throw new RuntimeException("Could not send message to Teams.", e);
        }
    }

    private void buildDetailsAttachment(Message msg, TeamsMessage teamsMessage) {
        teamsMessage.addDetailsAttachmentField(new TeamsMessage.AttachmentField("Stream Description", stream.getDescription(), false));
        teamsMessage.addDetailsAttachmentField(new TeamsMessage.AttachmentField("Source", msg.getSource(), true));

        for (Map.Entry<String, Object> field : msg.getFields().entrySet()) {
            if (Message.RESERVED_FIELDS.contains(field.getKey())) continue;
            teamsMessage.addDetailsAttachmentField(new TeamsMessage.AttachmentField(field.getKey(), field.getValue().toString(), true));
        }
    }

    private String buildFullMessageBody(Stream stream, Message msg) {
        String graylogUri = "";
        String titleLink;
        if (!isNullOrEmpty(graylogUri)) {
            titleLink = "<" + buildStreamLink(graylogUri, stream) + "|" + stream.getTitle() + ">";
        } else {
            titleLink = "_" + stream.getTitle() + "_";
        }

        String messageLink;
        if (!isNullOrEmpty(graylogUri)) {
            String index = "graylog_deflector"; // would use msg.getFieldAs(String.class, "_index"), but it returns null
            messageLink = "<" + buildMessageLink(graylogUri, index, msg.getId()) + "|New message>";
        } else {
            messageLink = "New message";
        }

        return String.format("*%s in Graylog stream %s*:\n> %s", messageLink, titleLink, msg.getMessage());
    }

    private String buildCustomMessage(Stream stream, Message msg, String template) {
        Map<String, Object> model = getModel(stream, msg);
        try {
            return templateEngine.transform(template, model);
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private Map<String, Object> getModel(Stream stream, Message msg) {
        Map<String, Object> model = new HashMap<>();

        String graylogUri = "";
        model.put("stream", stream);
        model.put("message", msg);

        if (!isNullOrEmpty(graylogUri)) {
            model.put("stream_url", buildStreamLink(graylogUri, stream));
        }

        return model;
    }

    private String buildShortMessageBody(Message msg) {
        String timeStamp = msg.getTimestamp().toDateTime(DateTimeZone.getDefault()).toString(DateTimeFormat.shortTime());
        return String.format("%s: %s", timeStamp, msg.getMessage());
    }

    @Override
    public void write(List<Message> list) {
        for (Message message : list) {
            write(message);
        }
    }

    public Map<String, Object> getConfiguration() {
        return configuration.getSource();
    }

    @FactoryClass
    public interface Factory extends MessageOutput.Factory<TeamsMessageOutput> {
        @Override
        TeamsMessageOutput create(Stream stream, Configuration configuration);

        @Override
        Config getConfig();

        @Override
        Descriptor getDescriptor();
    }

    @ConfigClass
    public static class Config extends MessageOutput.Config {
        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            return TeamsConfigurationRequestFactory.createTeamsMessageOutputConfigurationRequest();
        }
    }

    public static class Descriptor extends MessageOutput.Descriptor {
        public Descriptor() {
            super("Teams Output", false, "", "Writes messages to a Teams chat room.");
        }
    }
}
