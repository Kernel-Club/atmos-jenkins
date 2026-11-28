// Job DSL: the CI jobs of Atmos. Created by JCasC at every start of Jenkins.
// Both jobs run the same pipeline: Jenkinsfile in atmos-dev. Only develop publishes images (Jenkinsfile).

// atmos: every change of develop. The branch is fixed: SCM polling of a Pipeline job does not
// expand build parameters, so a branch like '*/${BRANCH}' never matches and no build starts.
pipelineJob('atmos') {
  description('CI of Atmos: checks, tests and images in Nexus for every change of develop. Pipeline: Jenkinsfile in atmos-dev.')

  definition {
    cpsScm {
      scm {
        git {
          remote {
            url('https://github.com/Kernel-Club/atmos-dev.git')   // public: no credential
          }
          branch('*/develop')
        }
      }
      scriptPath('Jenkinsfile')
      lightweight(true)
    }
  }

  properties {
    pipelineTriggers {
      triggers {
        pollSCM {
          scmpoll_spec('H/5 * * * *')   // check GitHub about every 5 minutes (no webhooks: Jenkins is not reachable from GitHub)
        }
      }
    }
  }
}

// atmos-branch: manual builds of a test branch (for example to prove that a check stops the build).
// No polling; the images are built but not published.
pipelineJob('atmos-branch') {
  description('Manual checks of a test branch of atmos-dev. Images are built, not published. Pipeline: Jenkinsfile in atmos-dev.')

  parameters {
    stringParam('BRANCH', '', 'Branch of atmos-dev to check, for example feature/new-check.')
  }

  definition {
    cpsScm {
      scm {
        git {
          remote {
            url('https://github.com/Kernel-Club/atmos-dev.git')
          }
          branch('*/${BRANCH}')
        }
      }
      scriptPath('Jenkinsfile')
      // Full checkout: a lightweight checkout cannot expand the BRANCH parameter reliably.
      lightweight(false)
    }
  }
}
