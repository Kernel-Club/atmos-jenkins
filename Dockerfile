# Jenkins controller image of the Atmos platform: pinned Jenkins, Docker CLI and pinned plugins.
ARG JENKINS_VERSION=2.580.2-lts-jdk21
FROM jenkins/jenkins:${JENKINS_VERSION}

# Docker CLI only: builds use the Docker Engine of the server through its socket.
# Versions match the Docker Engine installed on the servers.
ARG DOCKER_CLI_VERSION="5:29.10.0-1~*"
ARG BUILDX_VERSION="0.39.0-1~*"

USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl \
 && install -m 0755 -d /etc/apt/keyrings \
 && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
 && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    > /etc/apt/sources.list.d/docker.list \
 && apt-get update \
 && apt-get install -y --no-install-recommends \
    docker-ce-cli="${DOCKER_CLI_VERSION}" \
    docker-buildx-plugin="${BUILDX_VERSION}" \
 && rm -rf /var/lib/apt/lists/*
USER jenkins

# Plugins from the pinned list (all dependencies included, so nothing is resolved at build time).
COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Atmos look: copied to JENKINS_HOME/userContent at every start (".override" replaces older copies).
COPY --chown=jenkins:jenkins theme/atmos.css /usr/share/jenkins/ref/userContent/atmos.css.override
COPY --chown=jenkins:jenkins theme/atmos-logo.svg /usr/share/jenkins/ref/userContent/atmos-logo.svg.override

# Configuration as code instead of the setup wizard.
COPY --chown=jenkins:jenkins casc/ /var/jenkins_config/
ENV CASC_JENKINS_CONFIG=/var/jenkins_config/jenkins.yaml \
    JENKINS_JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
