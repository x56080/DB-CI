import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.page.impl.testbuild.BaseBuildImpl
import com.sequoiadb.ci.utils.impl.TestBuildConfigMgr
import hudson.AbortException

@Library("global_ci@main")
import com.sequoiadb.ci.common.RunMode
import com.sequoiadb.ci.page.PageOption
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.StatusUtil

@Library("db_ci@meger_ct")
import com.sequoiadb.ci.service.build.test.ReadyEnv
import com.sequoiadb.ci.service.build.test.BuildEnv
import com.sequoiadb.ci.service.build.test.BuildAnt
import com.sequoiadb.ci.page.impl.testbuild.BaseBuildImpl

CommonUtil commonUtil = null
StatusUtil statusUtil = null
ConfigMgr configMgr = null
ReadyEnv readyEnv = null

pipeline {
    agent {
        label "master"
    }

    environment {
        RUN_MODE = "${RunMode.base_build}"
        BUILD_MODE = "testbuild"
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
                        configMgr = new TestBuildConfigMgr(commonUtil);
                        configMgr.loadConf();
                    } catch (AbortException e) {
                        statusUtil.abort(e)
                    } catch (Exception e) {
                        statusUtil.failure(e)
                    }
                }
            }
        }

        stage('Test Stage') {
            steps {
                script {
                    readyEnv = new ReadyEnv(commonUtil, configMgr)
                    readyEnv.node({
                        cleanWs()

                        stage('CheckBuildEnv') {
                            readyEnv.readyScript()
                            readyEnv.copyArchive()
                            readyEnv.lock()
                        }

                        stage('ResetBuildEnv') {
                            BuildEnv buildEnv = new BuildEnv(commonUtil, configMgr)
                            buildEnv.reset(readyEnv)
                        }

                        stage('InvokeAnt') {
                            BuildAnt buildAnt = new BuildAnt(commonUtil, configMgr)
                            buildAnt.call(readyEnv)
                            buildAnt.junit()
                        }
                    })
                }
            }
            post {
                always {
                    script { readyEnv.node({ readyEnv.unlock() }) }
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
