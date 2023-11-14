package com.sequoiadb.ci.service.build

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.service.entry.CompileType
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr

class CallTestStage {

    private CommonUtil util = null
    private ConfigMgr mgr = null


    CallTestStage(CommonUtil commonUtil, ConfigMgr configMgr) {
        this.util = commonUtil
        this.mgr = configMgr
    }


    def callTest() {
        def item = util.getEnv("${ExtArgs.COMPILE_TYPE}", 'abc.x86')
        def compileType = new CompileType(item)
        call(compileType.arch)
    }


    private def call(String arch) {
        def ret = [:]
        def params = [util.stringVal("${ExtArgs.BUILD_NUMBER}", util.getEnv("${ExtArgs.BUILD_NUMBER}"))]

        if (!util.isEnvAttrEmpty("${ExtArgs.GIT_SHA}")) {
            params.add(util.stringVal("${ExtArgs.GIT_SHA}", util.getEnv("${ExtArgs.GIT_SHA}")))
        }

        def list = mgr.get('testproject') as List<String>
        for (final def item in list) {
            final String name = "${item}_${arch}"
            final def stageName = name.replace("_", " ").replace("test", "").replace(arch, "")
            ret.put(item, {
                util.stage2(stageName, {
                    util.build(name, params)
                })
            })
        }
        util.parallel(ret)
    }
}
