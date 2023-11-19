package com.sequoiadb.ci.page.impl.conbinebuild

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.Impl.SubExtArgs
import com.sequoiadb.ci.common.RunMode
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.page.PageOption

class DailyBuildImpl implements IPageOption {

    private PageOption pageOption

    DailyBuildImpl(PageOption pageOption) {
        this.pageOption = pageOption
    }

    def getChoiceOptionOfParam(ExtArgs optionEnum, String key, String defaultVal = null) {
        def branchList = pageOption.configMgr.get(key) as List<String>
        def option = []


        if (defaultVal != null) option.add(defaultVal)

        //过滤页面编译类型的选项通过架构
        String empty = 'null'
        String arch = pageOption.util.getEnv("${ExtArgs.COMPILE_ARCH}", empty)

        for (final def item in branchList) {
            def split = item.split("\\.")
            if (split.length < 2) continue
            // COMPILE_ARCH 设置为空则将会不会根据设置架构选项进行过滤
            def isArch = arch == empty ? true : arch == split[1]
            if (isArch && !option.contains(item)) option.add(item)
        }

        return pageOption.util.choice("${optionEnum}", option)
    }

    @Override
    def getPageArgs() {
        def params = []
        def supportMap = [
            "$ExtArgs.BRANCH"      : "supportBranch",
            "$ExtArgs.COMPILE_TYPE": "supportCompileTypes",
            "$ExtArgs.TEST_PROJECT": "supportTestType",
        ]

        params.add(getChoiceOptionOfParam(ExtArgs.COMPILE_TYPE, supportMap.get("$ExtArgs.COMPILE_TYPE"), pageOption.util.getEnv("${ExtArgs.COMPILE_TYPE}")))
        params.add(pageOption.util.stringDefaultVal("${ExtArgs.BRANCH}", pageOption.util.getEnv("${ExtArgs.BRANCH}")))
        params.add(pageOption.util.string("${ExtArgs.GIT_SHA}"))
        params.add(pageOption.util.stringDefaultVal("${ExtArgs.JOB_NUMBER}", pageOption.util.getEnv("${ExtArgs.JOB_NUMBER}")))
        params.add(pageOption.util.booleanParamDefaultVal("${ExtArgs.COMPILE_DOC}", pageOption.util.getEnvToBoolean("${ExtArgs.COMPILE_DOC}")))

        def mode = pageOption.util.getEnv("${ExtArgs.RUN_MODE}")
        if (RunMode.daily_build.toString() == mode || RunMode.release_build.toString() == mode) {
            params.add(pageOption.util.booleanParamDefaultVal("${ExtArgs.EXECUTE_TEST}", pageOption.util.getEnvToBoolean("${ExtArgs.EXECUTE_TEST}")))

            def testListStr = ""
            def testList = pageOption.configMgr.get(supportMap.get("$ExtArgs.TEST_PROJECT")) as List<String>
            for (final def item in testList) testListStr += "$item,"
            params.add(pageOption.util.multiChoice("${SubExtArgs.TEST_PROJECT_LIST}", testListStr, testListStr))
        }

        def pageOptionList = []
        pageOptionList.add(pageOption.util.parameters(params))
        pageOptionList.add(pageOption.getBuildDiscarderOfOption())
        pageOptionList.add(pageOption.util.disableConcurrentBuilds())
        if (null != pageOption.getCronOfOption()) pageOptionList.add(pageOption.getCronOfOption())

        return pageOptionList
    }
}
