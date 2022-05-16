FROM jboss/wildfly:16.0.0.Final
ADD ./dockerWarVolume/rfdashboard.war /opt/jboss/wildfly/standalone/deployments