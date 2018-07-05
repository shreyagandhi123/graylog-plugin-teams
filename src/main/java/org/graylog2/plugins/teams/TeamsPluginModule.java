package org.graylog2.plugins.teams;

import org.graylog2.plugins.teams.callback.TeamsAlarmCallback;
import org.graylog2.plugins.teams.output.TeamsMessageOutput;
import org.graylog2.plugin.PluginModule;

public class TeamsPluginModule extends PluginModule {
    @Override
    protected void configure() {
        addAlarmCallback(TeamsAlarmCallback.class);
        addMessageOutput(TeamsMessageOutput.class);
    }
}
