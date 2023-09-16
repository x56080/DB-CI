import com.sequoiadb.ci.SelfScript
import com.sequoiadb.ci.util.CommonUtil
import com.sequoiadb.ci.util.StatusUtil

@Library("compile_db@global") _
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
        stage("Build M2S") {
            steps {
                script {
                    SelfScript.init(this)
                    node("${env.compile_label}") {
                        try {
                            CommonUtil.gitClone("${env.git_dir}", "${env.git_url}", "${env.git_branch}", true)
                            dir("${WORKSPACE}/${env.git_dir}") {
                                def status = sh(script: 'python3 build.py', returnStatus: true) == 0
                                if (!status) throw new Exception('pipeline build failure')
                                archiveArtifacts(artifacts: "${env.compile_archive}", onlyIfSuccessful: true)
                            }
                        } catch (Exception e) {
                            StatusUtil.status(StatusUtil.Status.FAILURE, e.message)
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
