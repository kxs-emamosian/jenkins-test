// seed/jobs.groovy
final REPO_OWNER = 'kxs-emamosian'
final REPO_NAME  = 'jenkins-test'

folder('projects')

readFileFromWorkspace('pipeline-paths.txt')
    .readLines()
    .findAll { it.trim() }
    .collect { it.trim().split('/') }
    .each { parts ->
        def project  = parts[1]
        def pipeline = parts[3]

        folder("projects/${project}")
        folder("projects/${project}/pipelines")

        multibranchPipelineJob("projects/${project}/pipelines/${pipeline}") {
            branchSources {
                github {
                    id("${project}-${pipeline}")
                    repoOwner(REPO_OWNER)
                    repository(REPO_NAME)
                    buildOriginBranch(true)
                    buildOriginPRMerge(true)
                }
            }
            factory {
                workflowBranchProjectFactory {
                    scriptPath("projects/${project}/pipelines/${pipeline}/Jenkinsfile")
                }
            }
            orphanedItemStrategy {
                discardOldItems { numToKeep(10) }
            }
        }
    }