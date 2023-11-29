package com.sequoiadb.ci.page.impl.testbuild

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.SubExtArgs
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.page.PageOption

class BaseBuildImpl implements IPageOption{

    private PageOption pageOption

    BaseBuildImpl(PageOption pageOption) {
        this.pageOption = pageOption
    }

    @Override
    def getPageArgs() {
        def params = []
        def supportMap = [
            "$ExtArgs.BRANCH"      : "supportBranch",
            "$ExtArgs.TEST_ARCH"   : "supportArch",
            "$ExtArgs.TEST_PROJECT": "supportTestType",
        ]

        params.add(pageOption.getChoiceOptionOfParam(ExtArgs.BRANCH, supportMap.get("$ExtArgs.BRANCH"), pageOption.util.getEnv("${ExtArgs.BRANCH}")))
        params.add(pageOption.getChoiceOptionOfParam(ExtArgs.TEST_ARCH, supportMap.get("$ExtArgs.TEST_ARCH"), pageOption.util.getEnv("${ExtArgs.TEST_ARCH}")))
        params.add(pageOption.getChoiceOptionOfParam(ExtArgs.TEST_PROJECT, supportMap.get("$ExtArgs.TEST_PROJECT"), pageOption.util.getEnv("${ExtArgs.TEST_PROJECT}")))
        params.add(pageOption.util.string("${ExtArgs.GIT_SHA}"))
        params.add(pageOption.util.string("${ExtArgs.DEPNAME}"))
        params.add(pageOption.util.string("${SubExtArgs.SDB_BUILD_NUMBER}"))

        def pageOptionList = []
        pageOptionList.add(pageOption.util.parameters(params))
        pageOptionList.add(pageOption.getBuildDiscarderOfOption())

        return pageOptionList
    }
}
