# CEBAF RF Dashboard (rfdashboard)
A Java EE 8 web application for high level display of the performance of CEBAF's RF and related systems.  This
application is build using the [Smoothness](https://github.com/JeffersonLab/smoothness) web template.

![Screenshot](https://github.com/JeffersonLab/rfdashboard/raw/master/Screenshot.png?raw=true "Screenshot")

## Overview

The RF Dashboard summarizes data from a variety of sources including the CED, MYA, custom data collection scripts, and
the LEM "what-if" tool called LEMSim.  The web interfaces provides a variety of charts, reports, and tables that aid in
understanding the performance of CEBAF's RF system at a point in time and identifying changes in performance over time.

## Quick Start with Compose
1. Grab project
    ```bash
    git clone https://github.com/JeffersonLab/rfdashboard
    cd rfdashboard
    ```
2. Connect to JLab network either directly or through VPN.  Some services have not yet been containerized.
3Launch Compose
    ```bash
    docker compose up
    ```

**Note**: Login with demo username "tbrown" and password "password".

**Note**: Logout functionality does not work unless you update the KEYCLOAK_FRONT_END_SERVER in the deps.yml 
and docker-compose.yml to the hostname of your development machine due to an upstream issue.  More comments in those
files.

**See**: [Docker Compose Strategy](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c) developed by
Ryan Slominski.

## Install
This application requires a Java 11+ JVM and standard library in addition to a Java EE 8+ application server.  This
application has been developed and tested with Wildfly.

**NOTE:** Two of the service dependencies are not met by deps.yml, MYA/myquery
and CED.  For the application to work, you will need to connect to a JLab network either directly or through the VPN.
Some docker configuration is left commented out for the mya/myquery dependency as that service has been containerized,
but interactions are more flexible over the VPN.  Since CED still requires the VPN, it makes more sense to leverage the
"real" service.

1. Install service [dependencies](https://github.com/JeffersonLab/rfdashboard/blob/master/deps.yml)
2. Download [Wildfly 26.1.3](https://www.wildfly.org/downloads/)
3. [Configure](https://github.com/JeffersonLab/rfdashboard#configure) Wildfly and start it
4. Download [rfdashboard.war](https://github.com/JeffersonLab/rfdashboard/releases) and deploy it to Wildfly
5. Connect to a JLab network or VPN.
6. Navigate your web browser to localhost:8080/RFDashboard

## Configure

### Configtime
Wildfly must be pre-configured before the first deployment of the app.  The 
[wildfly bash scripts](https://github.com/JeffersonLab/wildfly#configure) can be used to accomplish this.  See the 
[Dockerfile](https://github.com/JeffersonLab/rfdashboard/blob/master/Dockerfile) for an example.

### Runtime
Uses the [Smoothness Environment Variables](https://github.com/JeffersonLab/smoothness#global-runtime) plus the 
following application specific:

| Name             | Description                                                                        |
|------------------|------------------------------------------------------------------------------------|
| RFD_CED_URL      | (Optional) Base URL for the CED server.  Defaults to https://ced.acc.jlab.org      |
| RFD_MYQUERY_URL  | (Optional) Base URL for the myquery server. Defaults to  https://epicsweb.jlab.org |                                               

### Database
The RF Dashboard app requires an Oracle 18+ database with the following
[schema](https://github.com/JeffersonLab/rfdashboard/tree/master/docker/oracle/setup) installed.  The application server
hosting the RF Dashboard application must also be configured with a JNDI datasource.

## Build
This project is built with [Java 17](https://adoptium.net/) (compiled to Java 11 bytecode), and uses the
[Gradle 7](https://gradle.org/) build tool to automatically download dependencies and build the project from source:

```bash
git clone https://github.com/JeffersonLab/rfdashboard
cd rfdashboard
gradlew build
```
**Note**: If you do not already have Gradle installed, it will be installed automatically by the wrapper script included
in the source

**Note for JLab On-Site Users**: Jefferson Lab has an intercepting
[proxy](https://gist.github.com/slominskir/92c25a033db93a90184a5994e71d0b78)

**See**: [Docker Development Quick Reference](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c#development-quick-reference)

## Release
1. Bump the version number and release date in build.gradle and commit and push to GitHub (using [Semantic Versioning](https://semver.org/)).
2. Create a new release on the GitHub Releases page corresponding to the same version in the build.gradle.   The release should enumerate changes and link issues.   A war artifact can be attached to the release to facilitate easy installation by users.
3. Build and publish a new Docker image [from the GitHub tag](https://gist.github.com/slominskir/a7da801e8259f5974c978f9c3091d52c#8-build-an-image-based-of-github-tag). GitHub is configured to do this automatically on git push of semver tag (typically part of GitHub release) or the [Publish to DockerHub](https://github.com/JeffersonLab/rfdashboard/actions/workflows/docker-publish.yml) action can be manually triggered after selecting a tag.
4. Bump and commit quick start [image version](https://github.com/JeffersonLab/rfdashboard/blob/master/docker-compose.override.yml)
