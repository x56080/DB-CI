package com.sequoiadb.ci.page.impl.compilebuild

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.RunMode
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.page.PageOption

class BaseBuildImpl implements IPageOption {

    private PageOption pageOption

    BaseBuildImpl(PageOption pageOption) {
        this.pageOption = pageOption
    }

    def getChoiceOptionOfParam(ExtArgs optionEnum, String key, String defaultVal = null) {
        def branchList = pageOption.configMgr.get(key) as List<String>

        //过滤页面编译类型的选项通过架构
        String empty = 'null'
        String arch = pageOption.util.getEnv("${ExtArgs.COMPILE_ARCH}", empty)

        if (arch != empty) {
            for (final def item in branchList) {
                def split = item.split("\\.")
                if (split.length < 2) continue
                if (arch != split[1]) branchList.remove(item)
            }
        }

        if (defaultVal != null) {
            branchList.remove(defaultVal)
            branchList.add(0, defaultVal)
        }
        return pageOption.util.choice("${optionEnum}", branchList)
    }

    @Override
    def getPageArgs() {
        def params = []
        def supportMap = [
            "$ExtArgs.BRANCH"      : "supportBranch",
            "$ExtArgs.COMPILE_TYPE": "supportCompileTypes"
        ]

        params.add(pageOption.util.stringDefaultVal("${ExtArgs.BRANCH}", pageOption.util.getEnv("${ExtArgs.BRANCH}")))
        params.add(pageOption.util.string("${ExtArgs.GIT_SHA}"))
        params.add(getChoiceOptionOfParam(ExtArgs.COMPILE_TYPE, supportMap.get("$ExtArgs.COMPILE_TYPE"), pageOption.util.getEnv("${ExtArgs.COMPILE_TYPE}")))
        params.add(pageOption.util.stringDefaultVal("${ExtArgs.JOB_NUMBER}", pageOption.util.getEnv("${ExtArgs.JOB_NUMBER}")))
        params.add(pageOption.util.booleanParamDefaultVal("${ExtArgs.COMPILE_DOC}", pageOption.util.getEnvToBoolean("${ExtArgs.COMPILE_DOC}")))

        def mode = pageOption.util.getEnv("${ExtArgs.RUN_MODE}")
        if (RunMode.daily_build.toString() == mode || RunMode.release_build.toString() == mode) {
            params.add(pageOption.util.booleanParamDefaultVal("${ExtArgs.EXECUTE_TEST}",
                pageOption.util.getEnvToBoolean("${ExtArgs.EXECUTE_TEST}")))
        }

        def pageOptionList = []
        pageOptionList.add(pageOption.util.parameters(params))
        pageOptionList.add(pageOption.getBuildDiscarderOfOption())

        return pageOptionList
    }
}
