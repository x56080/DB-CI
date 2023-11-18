import com.sequoiadb.ci.service.entry.CompileType
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
import com.sequoiadb.ci.page.impl.compilebuild.CommitBuildImpl
import com.sequoiadb.ci.utils.impl.CompileBuildConfigMgr

CommonUtil commonUtil = null
StatusUtil statusUtil = null
ConfigMgr configMgr = null
List envList = [
    "RUN_MODE=${RunMode.commit_build.toString()}",
    "BUILD_MODE=compilebuild",
    "TEST_SH_PATH=script/compileTestcase.sh",
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
                }, statusUtil.isStatusNormal() && !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG"))


                commonUtil.stage('Compile Testcase', {
                    def typeStr = commonUtil.getEnv("$ExtArgs.COMPILE_TYPE")
                    def arch = new CompileType(typeStr).getArch()
                    def label = configMgr.get("machine/compilesdb/${arch}")

                    node(label) {
                        dir('script') {
                            checkout scm
                            println("test run node: $env.COMPILE_SDB_NODE\ntest script: $env.TEST_SH_PATH\ntarget dir: $WORKSPACE")
                            def isSuccess = sh(script: "bash $env.TEST_SH_PATH $WORKSPACE", returnStatus: true) == 0
                            if (!isSuccess) statusUtil.status(StatusUtil.Status.FAILURE, "exec test status：$isSuccess")
                        }
                    }
                }, statusUtil.isStatusNormal() && !commonUtil.getEnvToBoolean("$ExtArgs.PIPELINE_DEBUG"))

            } finally {
                stage('Post Stage') {
                    if (statusUtil.isStatusFailure()) commonUtil.emailext(true)
                    IPageOption pageOption = new PageOption(commonUtil, configMgr)
                    IPageOption buildImpl = new CommitBuildImpl(pageOption)
                    properties(buildImpl.getPageArgs())
                }
            }
        }
    }
}