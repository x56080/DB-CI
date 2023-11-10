package com.sequoiadb.ci.page.impl.testbuild

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.page.PageOption

class ReleaseBuildImpl implements IPageOption{

    private PageOption pageOption

    ReleaseBuildImpl(PageOption pageOption) {
        this.pageOption = pageOption
    }

    @Override
    def getPageArgs() {
        def pageOptionList = []
        def params = []
        def supportMap = ["$ExtArgs.BRANCH" : "supportBranch",];

        params.add(pageOption.getChoiceOptionOfParam(ExtArgs.BRANCH,supportMap.get("$ExtArgs.BRANCH")))
        params.add(pageOption.util.string("${ExtArgs.GIT_SHA}"))
        params.add(pageOption.util.booleanParamDefaultVal("${ExtArgs.EXECUTE_TEST}", true))
        pageOptionList.add(pageOption.getBuildDiscarderOfOption())
        pageOptionList.add(pageOption.util.parameters(params))

        return pageOptionList
    }
}
