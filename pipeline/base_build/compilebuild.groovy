import hudson.AbortException

@Library("global_ci@main")
import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.RunMode
import com.sequoiadb.ci.page.PageOption
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.StatusUtil

@Library("db_ci@global")
import com.sequoiadb.ci.service.build.CompileStage
import com.sequoiadb.ci.page.impl.compilebuild.*
import com.sequoiadb.ci.utils.impl.CompileBuildConfigMgr

CommonUtil commonUtil = null
StatusUtil statusUtil = null
ConfigMgr configMgr = null

node('master') {

    timestamps {

        withEnv(["RUN_MODE=${RunMode.base_build.toString()}", "BUILD_MODE=compilebuild"]) {

            try {
                stage("Init Stage", {
                    try {
                        commonUtil = new CommonUtil(this)
                        statusUtil = new StatusUtil(this)
                        configMgr = new CompileBuildConfigMgr(commonUtil)
                        configMgr.checkoutScm()
                        configMgr.loadConf()
                    } catch (AbortException e) {
                        statusUtil.abort(e)
                    } catch (Exception e) {
                        statusUtil.failure(e)
                    }
                })


                commonUtil.stage('Build Stage', {
                    try {
                        CompileStage compileStage = new CompileStage(commonUtil, configMgr)
                        compileStage.init()
                        compileStage.compile()
                    } catch (AbortException e) {
                        statusUtil.abort(e)
                    } catch (Exception e) {
                        statusUtil.failure(e)
                    }
                }, statusUtil.isStatusNormal() && !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG"))

            } finally {
                stage('Post Stage') {
                    IPageOption pageOption = new PageOption(commonUtil, configMgr)
                    IPageOption buildImpl = new BaseBuildImpl(pageOption)
                    properties(buildImpl.getPageArgs())
                }
            }
        }
    }
}

