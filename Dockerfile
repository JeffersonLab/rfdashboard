FROM jboss/wildfly:16.0.0.Final
ADD ./dockerWarVolume/RFDashboard.war /opt/jboss/wildfly/standalone/deployments
RUN /opt/jboss/wildfly/bin/add-user.sh admin password --silent
CMD /opt/jboss/wildfly/bin/standalone.sh -b=0.0.0.0 -bmanagement=0.0.0.0