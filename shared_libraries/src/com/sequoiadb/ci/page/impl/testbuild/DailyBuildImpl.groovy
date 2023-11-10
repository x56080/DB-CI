package com.sequoiadb.ci.page.impl.testbuild

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.page.PageOption

class DailyBuildImpl implements IPageOption{

    private PageOption pageOption

    DailyBuildImpl(PageOption pageOption) {
        this.pageOption = pageOption
    }

    @Override
    def getPageArgs() {
        def params = []
        def branch = pageOption.util.getEnv("$ExtArgs.BRANCH")
        params.add(pageOption.util.stringDefaultVal("$ExtArgs.BRANCH", branch))
        params.add(pageOption.util.string("${ExtArgs.GIT_SHA}"))
        params.add(pageOption.util.booleanParamDefaultVal("${ExtArgs.COMPILE_DOC}", true))

        // 通过ExtArgs.EXECUTE_TEST值设置下一次是否执行测试!EXECUTE_TEST
        boolean execute_test = pageOption.util.getEnvToBoolean("${ExtArgs.EXECUTE_TEST}")
        params.add(pageOption.util.booleanParamDefaultVal("${ExtArgs.EXECUTE_TEST}", !execute_test))

        def supportMap = [
            "$ExtArgs.BRANCH"      : "supportBranch",
            "$ExtArgs.TEST_SITE"   : "supportSite",
            "$ExtArgs.TEST_PROJECT": "supportProject",
        ]
        params.add(pageOption.getChoiceOptionOfParam(ExtArgs.TEST_SITE,supportMap.get("$ExtArgs.TEST_SITE")))
        params.add(pageOption.getChoiceOptionOfParam(ExtArgs.TEST_PROJECT,supportMap.get("$ExtArgs.TEST_PROJECT")))

        def pageOptionList = []
        pageOptionList.add(pageOption.util.parameters(params))
        pageOptionList.add(pageOption.getBuildDiscarderOfOption())
        pageOptionList.add(pageOption.getCronOfOption())

        return pageOptionList
    }
}
