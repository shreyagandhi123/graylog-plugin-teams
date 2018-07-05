package org.graylog2.plugins.teams;

import org.graylog2.plugins.teams.callback.TeamsAlarmCallback;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class TeamsPluginMetadata implements PluginMetaData {
    @Override
    public String getUniqueId() {
        return TeamsAlarmCallback.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "Teams";
    }

    @Override
    public String getAuthor() {
        return "Graylog, Inc.";
    }

    @Override
    public URI getURL() {
        return URI.create("https://www.graylog.org");
    }

    @Override
    public Version getVersion() {
        return new Version(3, 0, 0);
    }

    @Override
    public String getDescription() {
        return "Teams plugin to forward messages or write alarms to Teams chat rooms.";
    }

    @Override
    public Version getRequiredVersion() {
        return new Version(2, 0, 0);
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
