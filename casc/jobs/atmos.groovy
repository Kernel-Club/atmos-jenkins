// Job DSL: the CI job of Atmos. Created by JCasC at every start of Jenkins.
// The pipeline itself is the Jenkinsfile in atmos-dev.

pipelineJob('atmos') {
  description('CI of Atmos: lint, tests and images in Nexus for every change of develop. Pipeline: Jenkinsfile in atmos-dev.')

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
      lightweight(true)   // read only the Jenkinsfile first, not the whole repository
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
