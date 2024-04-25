package com.sequoiadb.ci.utils.impl

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.SubExtArgs
import com.sequoiadb.ci.exception.ExtArgsException
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr

class TestBuildConfigMgr extends  ConfigMgr{

    TestBuildConfigMgr(CommonUtil common) {
        super(common)
    }

    def checkoutScm(){
        //util.cleanWs()
        util.checkoutScm()
    }

    @Override
    def checkOutsideConf() {
        //0.检查必填
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
                case ExtArgs.TEST_PROJECT.toString():
                    this.checkContains(ExtArgs.TEST_PROJECT, "supportTestType")
                    break
                case ExtArgs.TEST_ARCH.toString():
                    this.checkContains(ExtArgs.TEST_ARCH, "supportArch")
                    break
            }
        }
    }
}
