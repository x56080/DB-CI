package com.sequoiadb.ci.service.build

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.SubExtArgs
import com.sequoiadb.ci.service.build.test.BuildAnt
import com.sequoiadb.ci.service.build.test.BuildEnv
import com.sequoiadb.ci.service.build.test.CollectInfo
import com.sequoiadb.ci.service.build.test.ReadyEnv
import com.sequoiadb.ci.service.entity.CompileType
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr

class CallTestStage {

    private CommonUtil util = null
    private ConfigMgr mgr = null


    CallTestStage(CommonUtil commonUtil, ConfigMgr configMgr) {
        this.util = commonUtil
        this.mgr = configMgr
    }

    /**
     * @description 调用测试子工程,不存在编译类型则默认调用x86架构的测试子工程
     * @return
     */
    def callTestJob() {
        def item = util.getEnv("${ExtArgs.COMPILE_TYPE}", 'abc.x86')
        def compileType = new CompileType(item)
        callJob(compileType.arch)
    }

    /**
     * @description 调用测试子工程,根据配置项testproject获取工程名称列表,名称格式: testproject.item+_+arch
     * @param arch
     * @return
     */
    private def callJob(String arch) {
        def ret = [:]
        def params = [
            util.stringVal("${ExtArgs.DEPNAME}", util.getEnv("${ExtArgs.JOB_NAME}")),
            util.stringVal("${SubExtArgs.SDB_BUILD_NUMBER}", util.getEnv("${ExtArgs.BUILD_NUMBER}")),
        ]

        if (!util.isEnvAttrEmpty("${ExtArgs.GIT_SHA}")) {
            params.add(util.stringVal("${ExtArgs.GIT_SHA}", util.getEnv("${ExtArgs.GIT_SHA}")))
        }

        def list = mgr.get('testproject') as List<String>
        for (final def item in list) {
            final String name = "${item}_${arch}"
            final def stageName = name.replace("_", " ").replace(arch, "")
            ret.put(item, {
                util.stage(stageName, {
                    util.build(name, params)
                })
            })
        }
        util.parallel(ret)
    }

    /**
     * @description 使用parallel方法并行执行闭包中不同类型测试工程的调用步骤
     * @return
     */
    def callTest() {
        def stageMap = [:]
        String compileTypeStr = util.getEnv("${ExtArgs.COMPILE_TYPE}")
        String arch = new CompileType(compileTypeStr).getArch()
        List typeList = (!util.isEnvAttrEmpty("${SubExtArgs.TEST_PROJECT_LIST}") ?
            util.getEnv("${SubExtArgs.TEST_PROJECT_LIST}").split(",") :
            mgr.get("supportTestType")) as List<String>

        for (final def item in typeList) {
            final def stageName = "test sdb ${item.toLowerCase()}"
            final def currentItem = item
            // 没有同其他并行任务中每一个任务都使用stage闭包,因为该调用方法中存在其他stage声明,所以此处声明则会出现冗余
            stageMap.put(stageName.toString(), { buildTest(currentItem, arch) })
        }
        util.parallel(stageMap)
    }


    def buildTest(String testType, String arch) {
        ReadyEnv readyEnv = new ReadyEnv(util, mgr)
        readyEnv.initCfg(testType, arch)

        String buildNum = util.getEnv("$ExtArgs.BUILD_NUMBER")
        String jobName = util.getEnv("$ExtArgs.JOB_NAME")

        BuildAnt buildAnt = null
        CollectInfo info = null
        String stageName = testType.toLowerCase().replace("_", ".")
        readyEnv.node({
            try {
                util.stage("ReEnv $stageName") {
                    util.cleanWs()
                    readyEnv.init()
                    readyEnv.readyScript()
                    readyEnv.copyArchive(jobName, buildNum)
                    //readyEnv.copyArchive()
                    readyEnv.lock()

                    BuildEnv buildEnv = new BuildEnv(util, mgr)
                    buildEnv.reset(readyEnv)
                }

                util.stage("InvAnt $stageName") {
                    buildAnt = new BuildAnt(util, mgr)
                    buildAnt.call(readyEnv)

                    info = new CollectInfo(util, mgr)
                    info.init(readyEnv)
                    buildAnt.junit()

                    if (!buildAnt.getState()) {
                        info.collectLogs(testType)
                    }
                }
            } finally {
                readyEnv.unlock()
            }
        })
    }
}
