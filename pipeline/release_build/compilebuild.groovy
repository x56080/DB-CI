import com.sequoiadb.ci.common.SubExtArgs
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException
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
import com.sequoiadb.ci.service.build.CallTestStage
import com.sequoiadb.ci.service.build.compile.CollectArchive
import com.sequoiadb.ci.utils.impl.CompileBuildConfigMgr

CommonUtil commonUtil = null
StatusUtil statusUtil = null
ConfigMgr configMgr = null
CollectArchive collectArchive = null
List envList = [
    "RUN_MODE=${RunMode.release_build.toString()}",
    "BUILD_MODE=compilebuild",
    "COMPILE_DOC=true",
]

node('master') {

    timestamps {

        withEnv(envList) {

            try {

                stage('Init Stage', {
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
                }, !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG") &&
                    statusUtil.isStatusNormal())


                commonUtil.stage('Collect Artifacts', {
                    try {
                        collectArchive = new CollectArchive(commonUtil, configMgr)
                        collectArchive.init()
                        collectArchive.call()
                    } catch (AbortException e) {
                        statusUtil.unstable(e)
                    } catch (Exception e) {
                        statusUtil.failure(e)
                    }
                }, !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG") &&
                    statusUtil.isStatusNormal())


                commonUtil.stage('Test Stage', {
                    try {
                        CallTestStage callTestStage = new CallTestStage(commonUtil, configMgr)
                        callTestStage.callTestJob()
                    } catch (AbortException e) {
                        statusUtil.abort(e)
                    } catch (FlowInterruptedException e) {
                        statusUtil.status(e.getResult().toString(), e.message)
                    } catch (Exception e) {
                        statusUtil.failure(e)
                    }
                }, !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG") &&
                    statusUtil.isStatusNormal())


                commonUtil.stage('Archive Stage', {
                    try {
                        collectArchive.archive()
                    } catch (Exception e) {
                        statusUtil.unstable(e)
                    }
                }, !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG") &&
                    commonUtil.getEnvToBoolean("$SubExtArgs.ARCHIVE") &&
                    statusUtil.isStatusNormal())

            } finally {
                stage('Post Stage') {
                    IPageOption pageOption = new PageOption(commonUtil, configMgr)
                    IPageOption buildImpl = new ReleaseBuildImpl(pageOption)
                    properties(buildImpl.getPageArgs())
                }
            }
        }
    }
}

