package org.jenkinsci.plugins.statsd;

import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.Descriptor;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

/**
 * Global Configuration for Statsd.
 */
@Extension
public class StatsdConfig extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(Descriptor.class.getName());

    private String prefix;
    private String host;
    private int port;
	private boolean includeNodeInName;

    public StatsdConfig() {
        load();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

	public boolean getIncludeNodeInName() {
		return includeNodeInName;
	}

	public void isIncludeNodeInName(boolean includeNodeInName) {
		this.includeNodeInName = includeNodeInName;
	}

	@Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        this.prefix = formData.getString("prefix");
        this.host = formData.getString("host");
        this.port = formData.getInt("port");
		this.includeNodeInName = formData.optBoolean("includeNodeInName");

        save();
        return super.configure(req,formData);
    }

    public static StatsdConfig get() {
        return GlobalConfiguration.all().get(StatsdConfig.class);
    }

    // @TODO: implement form validation

}
