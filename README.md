# atmos-jenkins

CI pipeline of the on-premise platform of **Atmos**: checks every change of the application and publishes versioned images to Nexus.

```text
atmos-dev (develop) ─► Jenkins: lint → tests → build ─► nexus.atmos.lan:5000 ─► Flux
```

## Pipeline

| Stage | What happens |
|---|---|
| Checkout | `develop` of [atmos-dev](https://github.com/Kernel-Club/atmos-dev), SCM polling, read-only deploy key |
| Lint, tests | A failed check stops the build |
| Build | Backend and frontend images; frontend uses the API address `/api` |
| Push | `nexus.atmos.lan:5000/atmos-{backend,frontend}:<build>-<short-sha>` |
| Cleanup | Local images and build cache are removed |

Tag example: `42-a1b2c3d`. Flux selects the image with the highest build number. After a rebuild of the `jenkins` VM, set **Next Build Number** of the job above the highest tag in Nexus, otherwise Flux keeps the old image.

## Setup in Jenkins

Jenkins runs on the `jenkins` VM and builds with the Docker Engine of that VM; Nexus is on the separate `nexus-traefik` VM. Both are installed by [atmos-ansible](https://github.com/Kernel-Club/atmos-ansible). The job is created by hand:

1. Credentials: the deploy key for `atmos-dev` and the Nexus push user.
2. Job type "Pipeline script from SCM", pointing to this repository (`Jenkinsfile`).
3. Trigger: SCM polling (GitHub webhooks cannot reach the home network).

Open Jenkins at `https://jenkins.atmos.lan` (through Traefik on `nexus-traefik`).

## Security

- Jenkins does CI only: no kubeconfig, no access to the cluster or to `atmos-k8s`.
- The Docker socket gives Jenkins root only on the `jenkins` VM, not on Nexus or Traefik.
- Credentials live in the Jenkins Credentials store, never in this repository (see `.gitignore`).

## Related

| Repository | Role |
|---|---|
| [atmos-dev](https://github.com/Kernel-Club/atmos-dev) | Application source and on-premise docs (`docs/on-premise/`) |
| [atmos-ansible](https://github.com/Kernel-Club/atmos-ansible) | Installs Jenkins, Nexus and Traefik |
| [atmos-k8s](https://github.com/Kernel-Club/atmos-k8s) | Flux deploys the published images |

Branching: `main` + `feature/*` / `fix/*` with a Pull Request ([branching model](https://github.com/Kernel-Club/atmos-dev/blob/develop/docs/on-premise/branching-model.md)).
