package org.graylog2.plugins.teams;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

public class TeamsPlugin implements Plugin {
    @Override
    public Collection<PluginModule> modules() {
        return Collections.singleton(new TeamsPluginModule());
    }

    @Override
    public PluginMetaData metadata() {
        return new TeamsPluginMetadata();
    }
}
