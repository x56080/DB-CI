import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.page.impl.compilebuild.CommitBuildImpl
import com.sequoiadb.ci.utils.impl.CompileBuildConfigMgr
import hudson.AbortException

@Library("global_ci@main")
import com.sequoiadb.ci.common.RunMode
import com.sequoiadb.ci.page.PageOption
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.StatusUtil

@Library("db_ci@global")
import com.sequoiadb.ci.service.build.CompileStage

CommonUtil commonUtil = null
StatusUtil statusUtil = null
ConfigMgr configMgr = null

pipeline {
    agent {
        label "master"
    }

    environment {
        RUN_MODE = "${RunMode.commit_build}"
        BUILD_MODE = "compilebuild"
        TEST_SH_PATH = 'script/pipelines/commit_build/script/compileTestcase.sh'
    }

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    stages {
        stage("Init Stage") {
            steps {
                script {
                    try {
                        commonUtil = new CommonUtil(this);
                        statusUtil = new StatusUtil(this);
                        configMgr = new CompileBuildConfigMgr(commonUtil);
                        configMgr.loadConf();
                    } catch (AbortException e) {
                        statusUtil.abort(e)
                    } catch (Exception e) {
                        statusUtil.failure(e)
                    }
                }
            }
        }

        stage('Build Stage') {
            when {
                expression {
                    statusUtil.isStatusNormal() &&
                        !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG")
                }
            }
            steps {
                script {
                    try {
                        CompileStage compileStage = new CompileStage(commonUtil, configMgr)
                        compileStage.init()
                        compileStage.compile()
                    } catch (AbortException e) {
                        statusUtil.abort(e)
                    } catch (Exception e) {
                        statusUtil.failure(e)
                    }
                }
            }
        }

        stage('Compile Testcase') {
            when {
                expression {
                    statusUtil.isStatusNormal() &&
                        !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG")
                }
            }
            steps {
                script {
                    def label = mgr.get("machine/compilesdb/${compileType.arch}")
                    node(env.COMPILE_SDB_NODE) {
                        dir('script') { checkout scm }
                        println("test run node: $env.COMPILE_SDB_NODE\ntest script: $env.TEST_SH_PATH\ntarget dir: $WORKSPACE")
                        def isSuccess = sh(script: "bash $env.TEST_SH_PATH $WORKSPACE", returnStatus: true) == 0
                        if (!isSuccess) statusUtil.status(StatusUtil.Status.ABORTED, "exec test status：$isSuccess")
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                IPageOption pageOption = new PageOption(commonUtil, configMgr)
                IPageOption baseBuildImpl = new CommitBuildImpl(pageOption)
                properties(baseBuildImpl.getPageArgs())
            }
        }
    }
}
