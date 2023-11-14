package com.sequoiadb.ci.page.impl.compilebuild

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.Impl.SubExtArgs
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.page.PageOption

class ReleaseBuildImpl implements IPageOption {

    private PageOption pageOption

    ReleaseBuildImpl(PageOption pageOption) {
        this.pageOption = pageOption
    }


    @Override
    def getPageArgs() {
        def params = []
        def supportMap = ["$ExtArgs.BRANCH": "supportBranch",]

        params.add(pageOption.getChoiceOptionOfParam(ExtArgs.BRANCH, supportMap.get("$ExtArgs.BRANCH"), pageOption.util.getEnv("${ExtArgs.BRANCH}")))
        params.add(pageOption.util.string("${ExtArgs.GIT_SHA}"))
        params.add(pageOption.util.booleanParamDefaultVal("${ExtArgs.EXECUTE_TEST}", pageOption.util.getEnvToBoolean("${ExtArgs.EXECUTE_TEST}")))
        params.add(pageOption.util.booleanParamDefaultVal("${SubExtArgs.ARCHIVE}", pageOption.util.getEnvToBoolean("${SubExtArgs.ARCHIVE}")))

        def pageOptionList = []
        pageOptionList.add(pageOption.util.parameters(params))
        pageOptionList.add(pageOption.getBuildDiscarderOfOption())
        pageOptionList.add(pageOption.util.disableConcurrentBuilds())

        return pageOptionList
    }
}
