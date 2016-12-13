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
        long duration = calculateDuration(r);
        String jobName = r.getParent().getFullName().toString();
        String result = r.getResult().toString();

        // sanitize jobName for statsd/graphite. based on: https://github.com/etsy/statsd/blob/v0.5.0/stats.js#L110-113
        jobName = jobName.replaceAll("\\s+", "_");
        jobName = jobName.replaceAll("\\.", "_");
        jobName = jobName.replaceAll("\\/", "-");
        jobName = jobName.replaceAll("[^a-zA-Z_\\-0-9]", "");

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
    /**
    * Returns the duration of the run. For pipeline jobs, {@link Run#getDuration()} always returns 0,
    * in this case this method will calculate the duration of the run by using the current time as the
    * end time.
    * @param run - A Run object representing a particular execution of Job.
    * @return - Return the duration of the run
   */
    private long calculateDuration(final Run r) {
        if (r.getDuration() != 0) {
            return r.getDuration();
        } else {
            long durationMillis = System.currentTimeMillis() - r.getStartTimeInMillis();
            return durationMillis;
        }
    }
}
