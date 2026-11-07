# project-atmos-onprem-jenkins

CI pipeline of the on-premise platform of **Project Atmos**: checks every change of the application and publishes versioned images to Nexus.

```text
project-atmos-dev (develop) ─► Jenkins: lint → tests → build ─► nexus.atmos.lan:5000 ─► Flux
```

## Pipeline

| Stage | What happens |
|---|---|
| Checkout | `develop` of [project-atmos-dev](https://github.com/Kernel-Club/project-atmos-dev), SCM polling, read-only deploy key |
| Lint, tests | A failed check stops the build |
| Build | Backend and frontend images; frontend uses the API address `/api` |
| Push | `nexus.atmos.lan:5000/project-atmos-{backend,frontend}:<build>-<short-sha>` |
| Cleanup | Local images and build cache are removed |

Tag example: `42-a1b2c3d`. Flux selects the image with the highest build number.

## Setup in Jenkins

Jenkins runs on the `tools` VM and is installed by [project-atmos-onprem-ansible](https://github.com/Kernel-Club/project-atmos-onprem-ansible). The job is created by hand:

1. Credentials: the deploy key for `project-atmos-dev` and the Nexus push user.
2. Job type "Pipeline script from SCM", pointing to this repository (`Jenkinsfile`).
3. Trigger: SCM polling (GitHub webhooks cannot reach the home network).

Open Jenkins at `https://jenkins.atmos.lan`.

## Security

- Jenkins does CI only: no kubeconfig, no access to the cluster or to `project-atmos-onprem-k3s`.
- Credentials live in the Jenkins Credentials store, never in this repository (see `.gitignore`).

## Related

| Repository | Role |
|---|---|
| [project-atmos-dev](https://github.com/Kernel-Club/project-atmos-dev) | Application source and on-premise docs (`docs/on-premise/`) |
| [project-atmos-onprem-ansible](https://github.com/Kernel-Club/project-atmos-onprem-ansible) | Installs Jenkins, Nexus and Traefik |
| [project-atmos-onprem-k3s](https://github.com/Kernel-Club/project-atmos-onprem-k3s) | Flux deploys the published images |

Branching: `main` + `feature/*` / `fix/*` with a Pull Request ([branching model](https://github.com/Kernel-Club/project-atmos-dev/blob/develop/docs/on-premise/branching-model.md)).
