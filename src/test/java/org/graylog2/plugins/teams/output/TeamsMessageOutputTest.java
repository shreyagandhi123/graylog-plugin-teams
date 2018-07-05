package org.graylog2.plugins.teams.output;

import com.floreysoft.jmte.Engine;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.outputs.MessageOutputConfigurationException;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class TeamsMessageOutputTest {
    private static final ImmutableMap<String, Object> VALID_CONFIG_SOURCE = ImmutableMap.<String, Object>builder()
            .put("@context", "https://schema.org/extensions")
            .put("@type", "MessageCard")
            .put("themeColor", "FF0000")
            .put("webhook_url", "https://outlook.office.com/webhook/9d969916-dd73-4b6b-a188-7154538d97c8@37f288ac-734b-4265-9187-18425e5894af/IncomingWebhook/202bd16d5b1f4cdc87cad933be3ae64e/00797f40-2bd0-4fca-959a-16170431aaef")
            .put("summary", "Test Alert")
            .put("title", "Graylog")
            .build();

    @Test
    public void testGetAttributes() throws MessageOutputConfigurationException {
        TeamsMessageOutput output = new TeamsMessageOutput(null, new Configuration(VALID_CONFIG_SOURCE), Engine.createDefaultEngine());

        final Map<String, Object> attributes = output.getConfiguration();
        assertThat(attributes.keySet(), hasItems("@context", "@type", "themeColor","summary","title","webhook_url"));
    }

     @Test
    public void checkConfigurationSucceedsWithValidConfiguration() throws MessageOutputConfigurationException{
        new TeamsMessageOutput(null, new Configuration(VALID_CONFIG_SOURCE), Engine.createDefaultEngine());
    }

    @Test(expected = MessageOutputConfigurationException.class)
    public void checkConfigurationFailsIfApiTokenIsMissing() throws MessageOutputConfigurationException {
        new TeamsMessageOutput(null, validConfigurationWithout("webhook_url"), Engine.createDefaultEngine());
    }

    @Test
    public void checkConfigurationFailsIfChannelDoesAcceptDirectMessages() throws MessageOutputConfigurationException{
        new TeamsMessageOutput(null, validConfigurationWithValue("@context", "http://schema.org/extensions"), Engine.createDefaultEngine());
    }

    @Test
    public void checkConfigurationFailsIfChannelIsMissing() throws MessageOutputConfigurationException{
        new TeamsMessageOutput(null, validConfigurationWithValue("@type", "MessageCard"), Engine.createDefaultEngine());
    }

       @Test
    public void checkConfigurationWorksWithCorrectProxyAddress() throws MessageOutputConfigurationException {
        new TeamsMessageOutput(null, validConfigurationWithValue("proxy_address", "http://127.0.0.1:1080"),
                Engine.createDefaultEngine());
    }

     @Test(expected = MessageOutputConfigurationException.class)
    public void checkConfigurationFailsIfProxyAddressIsInvalid() throws MessageOutputConfigurationException {
        new TeamsMessageOutput(null, validConfigurationWithValue("proxy_address", "Definitely$$Not#A!!URL"),
                Engine.createDefaultEngine());
    }

    @Test(expected = MessageOutputConfigurationException.class)
    public void checkConfigurationFailsIfProxyAddressIsMissingAPort() throws MessageOutputConfigurationException {
        new TeamsMessageOutput(null, validConfigurationWithValue("proxy_address", "127.0.0.1"),
                Engine.createDefaultEngine());
    }

    @Test(expected = MessageOutputConfigurationException.class)
    public void checkConfigurationFailsIfProxyAddressHasWrongFormat() throws MessageOutputConfigurationException {
        new TeamsMessageOutput(null, validConfigurationWithValue("proxy_address", "vpn://127.0.0.1"),
                Engine.createDefaultEngine());
    }

    private Configuration validConfigurationWithout(final String key) {
        return new Configuration(Maps.filterEntries(VALID_CONFIG_SOURCE, new Predicate<Map.Entry<String, Object>>() {
            @Override
            public boolean apply(Map.Entry<String, Object> input) {
                return key.equals(input.getKey());
            }
        }));
    }

    private Configuration validConfigurationWithValue(String key, String value) {
        Map<String, Object> confCopy = Maps.newHashMap(VALID_CONFIG_SOURCE);
        confCopy.put(key, value);

        return new Configuration(confCopy);
    }

}
