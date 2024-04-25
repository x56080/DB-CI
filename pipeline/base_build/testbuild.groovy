@Library("global_ci@main")
import hudson.AbortException
import com.sequoiadb.ci.common.RunMode
import com.sequoiadb.ci.page.PageOption
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.StatusUtil

@Library("db_ci@global")
import com.sequoiadb.ci.service.build.test.ReadyEnv
import com.sequoiadb.ci.service.build.test.BuildEnv
import com.sequoiadb.ci.service.build.test.BuildAnt
import com.sequoiadb.ci.service.build.test.CollectInfo
import com.sequoiadb.ci.page.impl.testbuild.BaseBuildImpl
import com.sequoiadb.ci.utils.impl.TestBuildConfigMgr

CommonUtil commonUtil = null
StatusUtil statusUtil = null
ConfigMgr configMgr = null
ReadyEnv readyEnv = null

node('master') {

    timestamps {

        withEnv(["RUN_MODE=${RunMode.base_build.toString()}", "BUILD_MODE=testbuild"]) {

            try {
                stage("Init Stage") {
                    try {
                        commonUtil = new CommonUtil(this)
                        statusUtil = new StatusUtil(this)
                        configMgr = new TestBuildConfigMgr(commonUtil)
                        configMgr.checkoutScm()
                        configMgr.loadConf()
                    } catch (AbortException e) {
                        statusUtil.abort(e)
                    } catch (Exception e) {
                        statusUtil.failure(e)
                    }
                }

                readyEnv = new ReadyEnv(commonUtil, configMgr)
                readyEnv.initCfg()
                readyEnv.node({
                    //cleanWs()
                    readyEnv.init()

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

                    CollectInfo collect = new CollectInfo(commonUtil, configMgr)
                    collect.init(readyEnv)
                    commonUtil.stage('CollectLog', {
                        collect.collectLogs()
                    }, !collect.getStateByReport())

                })
            } finally {
                stage('Post Stage') {
                    if (readyEnv != null) readyEnv.node({ readyEnv.unlock() })
                    IPageOption pageOption = new PageOption(commonUtil, configMgr)
                    IPageOption buildImpl = new BaseBuildImpl(pageOption)
                    properties(buildImpl.getPageArgs())
                }
            }
        }
    }
}
