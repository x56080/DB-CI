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
import com.sequoiadb.ci.page.impl.compilebuild.DailyBuildImpl
import com.sequoiadb.ci.service.build.CallTestStage
import com.sequoiadb.ci.utils.impl.CompileBuildConfigMgr

CommonUtil commonUtil = null
StatusUtil statusUtil = null
ConfigMgr configMgr = null

node('master') {

    timestamps {

        withEnv(["RUN_MODE=${RunMode.daily_build.toString()}", "BUILD_MODE=compilebuild"]) {
            try {

                stage("Init Stage") {
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
                }

                stage('Build Stage') {
                    if (statusUtil.isStatusNormal() && !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG")) {
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

                stage('Test Stage') {
                    if (!commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG") && commonUtil.getEnvToBoolean("$ExtArgs.EXECUTE_TEST")) {
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

            } finally {
                stage('Post Stage') {
                    IPageOption pageOption = new PageOption(commonUtil, configMgr)
                    IPageOption buildImpl = new DailyBuildImpl(pageOption)
                    properties(buildImpl.getPageArgs())
                }
            }
        }
    }
}