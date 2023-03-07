ARG BUILD_IMAGE=gradle:7.4-jdk17
ARG RUN_IMAGE=slominskir/smoothness-weblib:3.9.2

############ Stage 0
FROM ${BUILD_IMAGE} as builder
ARG CUSTOM_CRT_URL
USER root
WORKDIR /
RUN if [ -z "${CUSTOM_CRT_URL}" ] ; then echo "No custom cert needed"; else \
           wget -O /usr/local/share/ca-certificates/customcert.crt $CUSTOM_CRT_URL \
           && update-ca-certificates \
           && keytool -import -alias custom -file /usr/local/share/ca-certificates/customcert.crt -cacerts -storepass changeit -noprompt \
           && export OPTIONAL_CERT_ARG=--cert=/etc/ssl/certs/ca-certificates.crt \
    ; fi
COPY . /app
# TODO: What is OPTIONAL_CERT_ARG for?
RUN cd /app && gradle build -x test --no-watch-fs $OPTIONAL_CERT_ARG


############ Stage 1
FROM ${RUN_IMAGE} as runner
COPY --from=builder /app/docker/app/app-setup.env /
RUN /app-setup.sh /app-setup.env
COPY --from=builder /app/build/libs/* /opt/jboss/wildfly/standalone/deployments