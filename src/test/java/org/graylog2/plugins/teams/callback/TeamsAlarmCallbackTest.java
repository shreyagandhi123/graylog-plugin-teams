package org.graylog2.plugins.teams.callback;

import com.floreysoft.jmte.Engine;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class TeamsAlarmCallbackTest {
    private static final ImmutableMap<String, Object> VALID_CONFIG_SOURCE = ImmutableMap.<String, Object>builder()
            .put("@context", "https://schema.org/extensions")
            .put("@type", "MessageCard")
            .put("themeColor", "FF0000")
            .put("webhook_url", "https://outlook.office.com/webhook/9d969916-dd73-4b6b-a188-7154538d97c8@37f288ac-734b-4265-9187-18425e5894af/IncomingWebhook/202bd16d5b1f4cdc87cad933be3ae64e/00797f40-2bd0-4fca-959a-16170431aaef")
            .put("summary", "Test Alert")
            .put("title", "Graylog")
            .build();
    private TeamsAlarmCallback alarmCallback;

    @Before
    public void setUp() {
        alarmCallback = new TeamsAlarmCallback(Engine.createDefaultEngine());
    }

    @Test
    public void testInitialize() throws AlarmCallbackConfigurationException {
        final Configuration configuration = new Configuration(VALID_CONFIG_SOURCE);
        alarmCallback.initialize(configuration);
    }

    @Test
    public void testGetAttributes() throws AlarmCallbackConfigurationException {
        final Configuration configuration = new Configuration(VALID_CONFIG_SOURCE);
        alarmCallback.initialize(configuration);

        final Map<String, Object> attributes = alarmCallback.getAttributes();
        assertThat(attributes.keySet(), hasItems("@context", "@type","themeColor", "summary","title","webhook_url"));
    }

    @Test
    public void checkConfigurationSucceedsWithValidConfiguration()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(new Configuration(VALID_CONFIG_SOURCE));
    }

     @Test(expected = AlarmCallbackConfigurationException.class)
    public void checkConfigurationFailsIfApiTokenIsMissing() throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithout("webhook_url"));
    }

    @Test
    public void checkConfigurationFailsIfChannelDoesAcceptDirectMessages() throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithValue("@context", "http://schema.org/extensions"));
    }

    @Test
    public void checkConfigurationFailsIfChannelIsMissing() throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithValue("@type", "MessageCard"));
    }

     @Test
    public void checkConfigurationWorksWithCorrectProxyAddress() throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithValue("proxy_address", "https://127.0.0.1:1080"));
    }

    @Test(expected = AlarmCallbackConfigurationException.class)
    public void checkConfigurationFailsIfProxyAddressHasWrongScheme() throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithValue("proxy_address", "vpn://127.0.0.1"));
    }

     @Test(expected = AlarmCallbackConfigurationException.class)
    public void checkConfigurationFailsIfProxyAddressIsInvalid() throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithValue("proxy_address", "Definitely$$Not#A!!URL"));
    }

    @Test(expected = AlarmCallbackConfigurationException.class)
    public void checkConfigurationFailsIfProxyAddressIsMissingAPort() throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithValue("proxy_address", "127.0.0.1"));
    }

    @Test(expected = AlarmCallbackConfigurationException.class)
    public void checkConfigurationFailsIfProxyAddressHasWrongFormat() throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithValue("proxy_address", "vpn://127.0.0.1"));
    }

    @Test
    public void testGetRequestedConfiguration() {
        assertThat(alarmCallback.getRequestedConfiguration().asList().keySet(),
                hasItems("@context", "@type", "themeColor","summary","title","webhook_url"));
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
