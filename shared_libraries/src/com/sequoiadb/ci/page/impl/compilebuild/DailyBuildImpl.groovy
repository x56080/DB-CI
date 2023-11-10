package com.sequoiadb.ci.page.impl.compilebuild

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.page.PageOption

class DailyBuildImpl implements IPageOption{

    private PageOption pageOption

    DailyBuildImpl(PageOption pageOption) {
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
        def pageOptionList = new BaseBuildImpl(pageOption).getPageArgs()
        pageOptionList.add(pageOption.getCronOfOption())
        return pageOptionList
    }
}
