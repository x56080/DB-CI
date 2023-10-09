import hudson.AbortException

@Library("global_ci@main")
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.StatusUtil


CommonUtil commonUtil = null
StatusUtil statusUtil = null

pipeline {
    agent {
        label 'master'
    }

    environment {
        cron = 'H 4 * * *'
        git_url = 'http://gitlab.sequoiadb.com/sequoiadb/cluster-config.git'
        git_dir = 'cluster_config'
        git_branch = 'main'
        compile_label = 'compile_x86_1'
        compile_archive = 'build/sdb-dds-cc_*.tar.gz'
    }

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    stages {
        stage("Init Config") {
            steps {
                script {
                    try {
                        commonUtil = new CommonUtil(this);
                        statusUtil = new StatusUtil(this);
                    } catch (AbortException e) {
                        statusUtil.abort(e)
                    } catch (Exception e) {
                        statusUtil.failure(e)
                    }
                }
            }
        }
        stage("Build ClusterConfig") {
            steps {
                script {
                    node("${env.compile_label}") {
                        try {
                            def targetDir="${WORKSPACE}/${env.git_dir}"
                            commonUtil.gitClone(targetDir, "${env.git_url}", "${env.git_branch}", true)
                            dir(targetDir) {
                                def status = sh(script: 'python3 build.py', returnStatus: true) == 0
                                if (!status) throw new Exception('pipeline build failure')
                                archiveArtifacts(artifacts: "${env.compile_archive}", onlyIfSuccessful: true)
                            }
                        } catch (Exception e) {
                            statusUtil.failure(e)
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                properties([pipelineTriggers([cron(env.cron)])])
            }
        }
        failure {
            script {
                emailext(
                    body: '$DEFAULT_CONTENT',
                    subject: '$DEFAULT_SUBJECT',
                    to: '$DEFAULT_RECIPIENTS',
                    recipientProviders: [buildUser()]
                )
            }
        }
    }
}
