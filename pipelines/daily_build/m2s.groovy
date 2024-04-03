@Library("global_ci@main")
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.StatusUtil
import hudson.AbortException


CommonUtil commonUtil = null
StatusUtil statusUtil = null


pipeline {
    agent {
        label 'master'
    }

    environment {
        cron = 'H 4 * * *'
        git_url = 'http://gitlab.sequoiadb.com/sequoiadb/m2s.git'
        git_dir = 'm2s'
        git_branch = 'main'
        compile_label = 'compile_x86_1'
        compile_archive = 'build/m2s_*_linux_aarch64.tar.gz,build/m2s_*_linux_x86_64.tar.gz'
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
        stage("Build M2S") {
            steps {
                script {
                    node("${env.compile_label}") {
                        try {
                            def targetDir="${WORKSPACE}/${env.git_dir}"
                            def branch = commonUtil.getEnv("git_sha", env.git_branch as String)
                            commonUtil.gitClone(targetDir, "${env.git_url}", branch, false)
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
                def propertyMap = [
                    parameters([string(name: 'git_sha')]),
                    pipelineTriggers([cron(env.cron)]),
                ]
                properties(propertyMap)
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
