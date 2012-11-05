package org.jenkinsci.plugins.statsd;

import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.model.labels.LabelAtom;
import hudson.util.LogTaskListener;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global Configuration for Statsd.
 */
@Extension
public class StatsdConfig extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(Descriptor.class.getName());

    private String prefix;
    private String host;
    private int port;

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

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        this.prefix = formData.getString("prefix");
        this.host = formData.getString("host");
        this.port = formData.getInt("port");

        save();
        return super.configure(req,formData);
    }

    public static StatsdConfig get() {
        return GlobalConfiguration.all().get(StatsdConfig.class);
    }

    // @TODO: implement form validation

}
