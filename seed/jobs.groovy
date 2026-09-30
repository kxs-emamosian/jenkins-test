// seed/jobs.groovy
final REPO_OWNER = 'kxs-emamosian'
final REPO_NAME  = 'jenkins-test'
// final CRED_ID    = 'github-app-credentials'

// Discover every pipeline directory across ALL branches
def paths = [] as Set
def branches = 'git ls-remote --heads origin'.execute().text
    .readLines().collect { it.split('refs/heads/')[1] }

branches.each { branch ->
    def listing = ['git', 'ls-tree', '-r', '--name-only', "origin/${branch}"]
        .execute().text
    listing.readLines()
        .findAll { it ==~ $/projects/[^/]+/pipelines/[^/]+/Jenkinsfile/$ }
        .each { paths << it }
}

paths.collect { it.split('/') }.each { parts ->
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
                // credentialsId(CRED_ID)
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