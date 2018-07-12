package org.graylog2.plugins.teams;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

public class TeamsMessage {

    private final String context;
    private final String type;
    private final String themeColor;
    private final List<AttachmentField> detailFields;
    private String customMessage;
    private final String title;
    private final String summary;
    private final ArrayList sections;
    public TeamsMessage(
            String themeColor,
            String type,
            String context,
            String title,
            String summary,
            ArrayList sections
    ) {
        this.themeColor = themeColor;
        this.type = type;
        this.context = context;
        this.title = title;
        this.summary = summary;
        this.sections = sections; 
        this.detailFields = Lists.newArrayList();
        this.customMessage = null;
    }

    public String getJsonString() {
        final Map<String, Object> params = new HashMap<String, Object>() {{
            put("@context", context);
            put("@type", type);
            put("themeColor", themeColor);
            put("title", title);
            put("summary", summary);
            put("sections", sections);
        }};

        final List<Attachment> attachments = new ArrayList<>();
        if (!isNullOrEmpty(customMessage)) {
            final Attachment attachment = new Attachment(
                    themeColor,
                    customMessage,
                    "Custom Message",
                    "Custom Message:",
                    null
            );
            attachments.add(attachment);
        }

        if (!detailFields.isEmpty()) {
            final Attachment attachment = new Attachment(
                    themeColor,
                    null,
                    "Alert details",
                    "Alert Details:",
                    detailFields
            );
            attachments.add(attachment);
        }

        if (!attachments.isEmpty()) {
            params.put("attachments", attachments);
        }

        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not build payload JSON.", e);
        }
    }

    public void addDetailsAttachmentField(AttachmentField attachmentField) {
        this.detailFields.add(attachmentField);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {
        @JsonProperty
        public String fallback;
        @JsonProperty
        public String text;
        @JsonProperty
        public String pretext;
        @JsonProperty
        public String color;
        @JsonProperty
        public List<AttachmentField> fields;

        @JsonCreator
        public Attachment(String color, String text, String fallback, String pretext, List<AttachmentField> fields) {
            this.fallback = fallback;
            this.text = text;
            this.pretext = pretext;
            this.color = color;
            this.fields = fields;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttachmentField {
        @JsonProperty
        public String title;
        @JsonProperty
        public String value;
        @JsonProperty("short")
        public boolean isShort;

        @JsonCreator
        public AttachmentField(String title, String value, boolean isShort) {
            this.title = title;
            this.value = value;
            this.isShort = isShort;
        }
    }

}
