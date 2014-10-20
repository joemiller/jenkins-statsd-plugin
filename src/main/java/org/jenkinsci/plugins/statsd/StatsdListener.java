package org.jenkinsci.plugins.statsd;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

/**
 * Send jenkins result and duration of Jenkins jobs to a statsd server
 */
@Extension
public class StatsdListener extends RunListener<Run> {

	private static final char METRIC_SEPARATOR = '.';
	private static final Logger LOGGER = Logger.getLogger(StatsdListener.class.getName());

    @Override
    public void onCompleted(final Run r, final TaskListener listener) {
        StatsdConfig config = StatsdConfig.get();

        if (config.getHost() == "" || config.getPort() == 0) {
            // statsd is not configured
            return;
        }

        String host = config.getHost();
        int port = config.getPort();

        String jobName = r.getParent().getFullName().toString();
        String result = r.getResult().toString();
        long duration = r.getDuration();
		jobName = sanitizeName(jobName);

		String metricName = generateMetricName(r, config, jobName, result);

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
	 * @return The display name of the node that the job was run on.
	 */
	private String getNodeName(Run run) {
		return run.getExecutor().getOwner().getNode().getDisplayName();
	}

	/**
	 * @return The metric name. A concatenation of the configured prefix, the job name, the
	 * node's display name (only if configured to do so) and finally the result.
	 */
	private String generateMetricName(Run run, StatsdConfig config, String jobName, String result) {
		String metricName = config.getPrefix() + METRIC_SEPARATOR + jobName;
		if (config.isIncludeNodeInName()) {
			String nodeName = getNodeName(run);
			metricName = metricName + METRIC_SEPARATOR + sanitizeName(nodeName);
		}
		return metricName + METRIC_SEPARATOR + result;
	}

	/**
	 * Sanitize name for statsd/graphite. based on: https://github.com/etsy/statsd/blob/v0.5.0/stats.js#L110-113
	 */
	private String sanitizeName(String name) {
		name = name.replaceAll("\\s+", "_");
		name = name.replaceAll("\\.", "_");
		name = name.replaceAll("\\/", "-");
		name = name.replaceAll("[^a-zA-Z_\\-0-9]", "");
		return name;
	}
}
