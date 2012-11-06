jenkins-statsd-plugin
=====================

Send the results of Jenkins jobs to a statsd server.

The results of _all_ jobs are collected and sent to statsd. You do not need to
add this plugin to each job.

Upon each job completion, the following metrics will be sent:

    statsd.increment('job_name.RESULT', 1)
    statsd.timing('job_name.RESULT', DURATION)

Where RESULT is a valid Jenkins build status, eg: SUCCESS, FAILURE, etc. Duration
is the time taken to run the job.

Installation
------------

- Browse to Jenkins
- Manage Jenkins > Advanced > Upload Plugin > Choose File, upload `jenkins-statsd.hpi`

Configuration
-------------

- Browse to Jenkins
- Manage Jenkins > Configure System  (or: http://<jenkins>/configure)
- Under 'Statsd Configuration':
    - prefix (optional)
    - host (required)
    - port (required)

Contributing
------------

Based on: https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial

- Install maven
- Run a debug server on http://localhost:8080 - `mvn hpi.run`
- Package into a new `.hpi` - `mvn package`
- send a pull request

AUTHOR
------
@TODO

LICENSE
-------
@TODO
