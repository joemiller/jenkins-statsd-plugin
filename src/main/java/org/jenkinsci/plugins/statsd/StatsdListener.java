package org.jenkinsci.plugins.statsd;

import hudson.model.Run;
import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hudson.model.Result.*;

/**
 * Send jenkins result and duration of Jenkins jobs to a statsd server
 */
@Extension
public class StatsdListener extends RunListener<Run> {

    private static final Logger LOGGER = Logger.getLogger(StatsdListener.class.getName());

    @Override
    public void onCompleted(final Run r, final TaskListener listener) {
        StatsdConfig config = StatsdConfig.get();

        if (config.getHost() == "" || config.getPort() == 0) {
            // statsd is not configured
            return;
        }

        String prefix = config.getPrefix();
        String host = config.getHost();
        int port = config.getPort();

        String jobName = r.getParent().getFullName().toString();
        String result = r.getResult().toString();
        long duration = r.getDuration();

        String metricName = prefix + '.' + jobName + '.' + result;

        LOGGER.log(Level.INFO, "StatsdListener: config: " + config);
        LOGGER.log(Level.INFO, "StatsdListener: job: " + jobName + ", result: " + result +
                               ", duration: " + duration + ", metricName: " + metricName);

        try {
            StatsdClient statsd = new StatsdClient(host, port);
            statsd.increment(metricName);
            statsd.timing(metricName, (int)duration);
        } catch (UnknownHostException e) {
            LOGGER.log(Level.WARNING, "StatsdListener Unknown Host: ", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "StatsdListener IOException: ", e);
        }
    }

}
