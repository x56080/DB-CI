package com.sequoiadb.ci.utils.impl

import com.sequoiadb.ci.common.CommonConst
import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.Impl.SubExtArgs
import com.sequoiadb.ci.common.LogLevel
import com.sequoiadb.ci.common.RunMode
import com.sequoiadb.ci.exception.ExtArgsException
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import hudson.AbortException

class CompileBuildConfigMgr extends ConfigMgr {

    CompileBuildConfigMgr(CommonUtil common) {
        super(common)
    }

    def checkoutScm() {
        util.cleanWs()
        util.checkoutScm()
    }

    @Override
    def checkOutsideConf() {
        //0. 检查必填
        def mode = util.getEnv("${SubExtArgs.BUILD_MODE}")
        List<String> required = (this.getPublicCfg().outsideEnv as Map).get(mode).required
        if (util.isEnvAttrEmpty(required as String[])) {
            throw new ExtArgsException(ExtArgsException.Type.NO_COMPLETE.getMessage(required as String[]))
        }

        for (final def item in required) {
            switch (item) {
                case ExtArgs.BRANCH.toString():
                    this.checkContains(ExtArgs.BRANCH, "supportBranch")
                    break
                case ExtArgs.COMPILE_TYPE.toString():
                    this.checkContains(ExtArgs.COMPILE_TYPE, "supportCompileTypes")
                    break
            }
        }

        //1. 判断arch是否支持,若设置COMPILE_ARCH后需判断传入编译类型中架构是否相符
        if (!util.isEnvAttrEmpty(ExtArgs.COMPILE_TYPE.toString())
            && !util.isEnvAttrEmpty(ExtArgs.COMPILE_ARCH.toString())) {
            this.checkContains(ExtArgs.COMPILE_ARCH, "supportArch")

            def type = util.getEnv(ExtArgs.COMPILE_TYPE.toString())
            def arch = util.getEnv(ExtArgs.COMPILE_ARCH.toString())
            if (!type.contains(arch)) {
                throw new ExtArgsException(ExtArgsException.Type.NO_SUPPORT_OF_ARCH.getMessage(arch))
            }
        }
    }

}
