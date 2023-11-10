import com.sequoiadb.ci.common.ExtArgs
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
import com.sequoiadb.ci.page.impl.compilebuild.BaseBuildImpl

CommonUtil commonUtil = null
StatusUtil statusUtil = null
ConfigMgr configMgr = null

pipeline {
    agent {
        label "master"
    }

    environment {
        RUN_MODE = "${RunMode.base_build}"
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
    }

    post {
        always {
            script {
                IPageOption pageOption = new PageOption(commonUtil, configMgr)
                IPageOption baseBuildImpl = new BaseBuildImpl(pageOption)
                properties(baseBuildImpl.getPageArgs())
            }
        }
    }
}
