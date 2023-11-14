package com.sequoiadb.ci.page.impl.compilebuild

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
        def pageOptionList = new BaseBuildImpl(pageOption).getPageArgs()
        pageOptionList.add(pageOption.getCronOfOption())
        return pageOptionList
    }
}
