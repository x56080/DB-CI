package com.sequoiadb.ci.service.build

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.SubExtArgs
import com.sequoiadb.ci.common.RunMode
import com.sequoiadb.ci.service.build.compile.BuildRunPkg
import com.sequoiadb.ci.service.build.compile.CompileSdb
import com.sequoiadb.ci.service.entity.CompileType
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr


class CompileStage {

    private CommonUtil util = null
    private ConfigMgr mgr = null
    private List<CompileType> compileTypeList = []


    CompileStage(CommonUtil commonUtil, ConfigMgr configMgr) {
        this.util = commonUtil
        this.mgr = configMgr
    }

    /**
     * @description 初始化编译列表,通过构建模式判断编译列表项的获取方式
     * @return
     */
    def init() {
        def mode = util.getEnv("${ExtArgs.RUN_MODE}")
        if (mode == RunMode.release_build.toString()) {
            def list = mgr.get('callBuildSdbType') as List<String>
            for (final def item in list) compileTypeList.add(new CompileType(item))
        } else if (!util.isEnvAttrEmpty("${ExtArgs.COMPILE_TYPE}")) {
            def type = util.getEnv("${ExtArgs.COMPILE_TYPE}")
            compileTypeList.add(new CompileType(type))
        }

        for (final def item in compileTypeList) util.println("compile item: ${item.getName()}")
        if (compileTypeList.isEmpty()) throw new Exception('compile type list is null')
    }


    def compile() {
        def stageMap = [:]
        stageMap.put('failFast', true)

        def isCompileDoc = util.getEnvToBoolean("${ExtArgs.COMPILE_DOC}")
        def isCompileSdb = util.getEnv("${SubExtArgs.COMPILE_SDB}", 'true').toBoolean()
        if (isCompileDoc) {
            def item = compileTypeList.get(0)
            stageMap.put('compile_doc', {
                util.stage("CompileDoc", { compileDoc(item) })
            })
        }

        if (isCompileSdb) {
            for (final def item in compileTypeList) {
                final def currentItem = item
                final def stageName = new StringBuilder()
                for (final def snItem in item.getName().split("_")) {
                    stageName.append(snItem.getChars()[0]).append(".")
                }
                stageName.setLength(stageName.length() - 1)

                stageMap.put(item.getName(), {
                    util.stage("CompileSdb ${stageName.toString()}", { compileSdb(currentItem) })
                    util.stage("BuildRun ${stageName.toString()}", { buildRun(currentItem) })
                })
            }
        }
        util.parallel(stageMap)
    }


    private def compileDoc(CompileType compileType) {
        def compile = new CompileSdb(util, mgr)

        def label1 = mgr.get("machine/docs/${compileType.arch}")
        def label2 = mgr.get("machine/calldocs/${compileType.arch}")
        util.node(label1, {
            compile.initDoc()
            compile.cloneDoc()
            util.node(label2, {
                try {
                    compile.init()
                    compile.compileDoc(compileType.type, compileType.arch)
                } catch (Exception e) {
                    throw e
                } finally {
                    compile.archive()
                }
            })
        })
        util.println("complete compile doc")
    }


    private def compileSdb(CompileType compileType) {
        def compile = new CompileSdb(util, mgr)

        def label = mgr.get("machine/compilesdb/${compileType.arch}")
        util.node(label, {
            try {
                compile.init()
                compile.compileTar(compileType.type, compileType.arch)
            } catch (Exception e) {
                throw e
            } finally {
                compile.archive()
            }
        })
        util.println("complete compile sdb")
    }


    private def buildRun(CompileType compileType) {
        def pkg = new BuildRunPkg(util, mgr)
        def label = mgr.get("machine/buildrun/${compileType.arch}")

        util.node(label, {
            pkg.build(compileType.type, compileType.arch)
            pkg.archive()
        })
        util.println("complete build run")
    }


}
