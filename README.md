# atmos-jenkins

Jenkins platform of the on-premise version of **Atmos**: the Jenkins image, its plugins and its configuration as code. Jenkins starts already configured, without the setup wizard and without manual steps.

```text
atmos-jenkins (this repository, release tag)
  └─ atmos-ansible (roles/jenkins) clones the tag on the jenkins server
       ├─ builds the image atmos-jenkins:<commit>
       ├─ places the secrets from Ansible Vault as read-only files (/run/secrets)
       └─ runs the container → JCasC applies casc/jenkins.yaml and creates the job at every start
```

## Contents

| Path | What it does |
|---|---|
| `Dockerfile` | Pinned Jenkins LTS, Docker CLI (builds use the Docker Engine of the server through its socket), the plugins and the JCasC file |
| `plugins.txt` | All plugins with their dependencies, pinned like a lock file |
| `casc/jenkins.yaml` | Configuration as code (JCasC): security, executor, URL, credentials, appearance |
| `casc/jobs/atmos.groovy` | The CI job of Atmos (Job DSL) |
| `theme/` | Atmos colors and logo (CSS on top of the dark theme) |

## Configuration

| Setting | Value |
|---|---|
| Users | Only `admin`; no sign-up, no anonymous access |
| Security | CSRF protection on; inbound agent port off |
| Builds | One executor on the built-in node; build steps run in temporary containers |
| URL | `https://jenkins.atmos.lan/` (through Traefik on `nexus-traefik`) |
| Appearance | English for everyone; dark theme with the Atmos colors, logo and the name "Atmos CI"; stage graph on the job and build pages (Pipeline Graph View) |
| Credentials | `nexus-push`: user `jenkins-push`, pushes images to `nexus.atmos.lan:5000`; `sonar-token`: SonarQube Cloud token for the analysis of `atmos-dev` |
| Jobs | `develop-ci`: `Jenkinsfile` from `develop` of `atmos-dev`, polls GitHub about every 5 minutes, publishes images. `test-ci-branches`: manual build of a test branch (parameter `BRANCH`), images are not published. `develop-cd-checks`: checks of the deployed app (smoke, Playwright, ZAP), started by Flux through a webhook with a token |
| Metrics | `/prometheus/` (Prometheus plugin) answers without login; Traefik allows this path only from the cluster nodes (`atmos-ansible`) |

`atmos-dev` is public, so Jenkins checks it out over HTTPS without a GitHub credential. The pipeline (`Jenkinsfile`) lives in `atmos-dev`, next to the code it builds.

Changes made in the web interface are overwritten at the next start: the configuration lives only in this repository. The valid JCasC keys are listed in Jenkins under *Manage Jenkins → Configuration as Code → Documentation*.

## Secrets

This repository contains no secrets. `casc/jenkins.yaml` refers to them as `${NAME}`; the values are in Ansible Vault in `atmos-ansible` and reach Jenkins as files in `/run/secrets`.

| Variable | Used for |
|---|---|
| `JENKINS_ADMIN_PASSWORD` | password of `admin` |
| `NEXUS_PUSH_PASSWORD` | credential `nexus-push` |

## Release a change

1. Change the files in a `feature/*` or `fix/*` branch and open a Pull Request.
2. To test before the merge, set `jenkins_platform_version` in `atmos-ansible` (`group_vars/all/versions.yml`) to the branch and run `ansible-playbook site.yml --tags jenkins`.
3. After the merge, tag `main` (`v0.3.0`, ...), set `jenkins_platform_version` to the tag and run the playbook again.

Ansible builds a new image only for a new commit; the build history in the `jenkins_home` volume is kept.

## Update the plugins

Plugins get security fixes often. To update, list the latest versions of the direct plugins with the image of the pinned Jenkins version and replace `plugins.txt` with the result:

```bash
ssh atmos-jenkins
sudo docker run --rm jenkins/jenkins:2.580.2-lts-jdk21 jenkins-plugin-cli --no-download --list \
  --plugins configuration-as-code workflow-aggregator git credentials docker-workflow locale dark-theme simple-theme-plugin job-dsl prometheus warnings-ng htmlpublisher generic-webhook-trigger pipeline-graph-view
```

Copy the list under "All requested plugins" in the format `<id>:<version>`, then release as above. Jenkins shows the plugins with known vulnerabilities under *Manage Jenkins*.

## Security

- Jenkins does CI only: no kubeconfig, no access to the cluster or to `atmos-k8s`.
- The Docker socket gives Jenkins root rights on the `jenkins` server only, not on Nexus or Traefik.
- Jenkins is reachable only through Traefik: `ufw` on `jenkins` accepts port 8080 only from `nexus-traefik`.

## Related

| Repository | Role |
|---|---|
| [atmos-dev](https://github.com/Kernel-Club/atmos-dev) | Application, `Jenkinsfile` and on-premise docs (`docs/on-premise/`) |
| [atmos-ansible](https://github.com/Kernel-Club/atmos-ansible) | Creates the servers and deploys this platform (`roles/jenkins/`) |
| [atmos-k8s](https://github.com/Kernel-Club/atmos-k8s) | Flux deploys the published images |

Branching: `main` + `feature/*` / `fix/*` with a Pull Request ([branching model](https://github.com/Kernel-Club/atmos-dev/blob/develop/docs/on-premise/branching-model.md)).
