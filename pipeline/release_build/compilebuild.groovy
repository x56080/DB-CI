import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.service.build.CallTestStage
import com.sequoiadb.ci.service.build.CollectArchive
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
import com.sequoiadb.ci.page.impl.compilebuild.*
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException

CommonUtil commonUtil = null
StatusUtil statusUtil = null
ConfigMgr configMgr = null
CollectArchive collectArchive = null

pipeline {
    agent {
        label "master"
    }

    environment {
        RUN_MODE = "${RunMode.release_build}"
        BUILD_MODE = "compilebuild"
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

//        stage('Collect Artifacts') {
//            when {
//                expression {
//                    statusUtil.isStatusNormal() &&
//                        !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG")
//                }
//            }
//            steps {
//                script {
//                    try {
//                        collectArchive = new CollectArchive(commonUtil, configMgr)
//                        collectArchive.init()
//                        collectArchive.call()
//                    } catch (AbortException e) {
//                        statusUtil.unstable(e)
//                    } catch (Exception e) {
//                        statusUtil.failure(e)
//                    }
//                }
//            }
//        }

        stage('Test Stage') {
            when {
                expression {
                    statusUtil.isStatusNormal() &&
                        !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG") &&
                        commonUtil.getEnvToBoolean("$ExtArgs.EXECUTE_TEST")
                }
            }
            steps {
                script {
                    try {
                        CallTestStage callTestStage = new CallTestStage(commonUtil, configMgr)
                        callTestStage.callTest()
                    } catch (AbortException e) {
                        statusUtil.abort(e)
                    } catch (FlowInterruptedException e) {
                        statusUtil.status(e.getResult().toString(), e.message)
                    } catch (Exception e) {
                        statusUtil.failure(e)
                    }
                }
            }
        }

//        stage('Archive') {
//            when {
//                expression {
//                    statusUtil.isStatusNormal() &&
//                        !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG")
//                }
//            }
//            steps {
//                script {
//                    collectArchive.save()
//                }
//            }
//        }
    }

    post {
        always {
            script {
                IPageOption pageOption = new PageOption(commonUtil, configMgr)
                IPageOption buildImpl = new ReleaseBuildImpl(pageOption)
                properties(buildImpl.getPageArgs())
            }
        }
    }
}
