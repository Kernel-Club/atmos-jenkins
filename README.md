# project-atmos-onprem-jenkins

CI pipeline of the on-premise platform of Project Atmos.

## Purpose

Holds the `Jenkinsfile` for Jenkins on the `tools` VM. The Jenkins job is "Pipeline script from SCM" pointing to this repository.

The pipeline:

1. Checks out [project-atmos-dev](https://github.com/Kernel-Club/project-atmos-dev) (branch `develop`, polling enabled).
2. Runs lint and tests.
3. Builds the backend and frontend images.
4. Pushes the images to the Nexus registry with the tag `build-<number>-<sha6>`.

Jenkins does CI only. It has no access to the cluster: Flux deploys the new images.

## Planned content

- `Jenkinsfile` — the pipeline
- later: Jenkins configuration as code

## Secrets

Credentials (Nexus, GitHub) live in the Jenkins credential store, never in this repository (see `.gitignore`).

## Related repositories

- [project-atmos-dev](https://github.com/Kernel-Club/project-atmos-dev) — application and architecture docs (`docs/on-premise/`)
- [project-atmos-onprem-provision](https://github.com/Kernel-Club/project-atmos-onprem-provision) — installs Jenkins with Ansible
- [project-atmos-onprem-gitops](https://github.com/Kernel-Club/project-atmos-onprem-gitops) — Flux and the Helm chart

## Branching

`main` is the current pipeline. Changes go through `feature/*` or `fix/*` branches and a Pull Request to `main`. See `docs/on-premise/branching-model.md` in `project-atmos-dev`.
