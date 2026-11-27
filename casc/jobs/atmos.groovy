// Job DSL: the CI job of Atmos. Created by JCasC at every start of Jenkins.
// The pipeline itself is the Jenkinsfile in atmos-dev.

pipelineJob('atmos') {
  description('CI of Atmos: checks, tests and images in Nexus for every change of develop. Pipeline: Jenkinsfile in atmos-dev.')

  // Manual builds of a test branch (for example to prove that a check stops the build).
  // Polling always uses the default, develop. Only develop publishes images (Jenkinsfile).
  parameters {
    stringParam('BRANCH', 'develop', 'Branch of atmos-dev to build. Only develop publishes images.')
  }

  definition {
    cpsScm {
      scm {
        git {
          remote {
            url('https://github.com/Kernel-Club/atmos-dev.git')   // public: no credential
          }
          branch('*/${BRANCH}')
        }
      }
      scriptPath('Jenkinsfile')
      // Full checkout: a lightweight checkout cannot expand the BRANCH parameter reliably.
      lightweight(false)
    }
  }

  properties {
    pipelineTriggers {
      triggers {
        pollSCM {
          scmpoll_spec('H/5 * * * *')   // check GitHub about every 5 minutes (no webhooks)
        }
      }
    }
  }
}
