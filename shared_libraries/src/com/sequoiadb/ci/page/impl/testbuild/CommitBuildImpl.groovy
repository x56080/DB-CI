package com.sequoiadb.ci.page.impl.testbuild

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.page.IPageOption
import com.sequoiadb.ci.page.PageOption

class CommitBuildImpl implements IPageOption{

    private PageOption pageOption

    CommitBuildImpl(PageOption pageOption) {
        this.pageOption = pageOption
    }

    @Override
    def getPageArgs() {
        def branch = pageOption.util.getEnv("${ExtArgs.BRANCH}")
        def token = pageOption.util.getEnv("${ExtArgs.JOB_NAME}")

        def triggerCfg =
            [
                causeString              : 'Triggered on $ref',
                printContributedVariables: true,
                printPostContent         : true,
                silentResponse           : false,
                shouldNotFlattern        : false,
                regexpFilterExpression   : "refs/heads/$branch",
                regexpFilterText         : '$ref',
                token                    : "$token",
                tokenCredentialId        : '',
                genericVariables         : [
                    [defaultValue: '', key: 'ref', regexpFilter: '', value: '$.ref'],
                    [defaultValue: '', key: 'event_name', regexpFilter: '', value: '$.event_name'],
                    [defaultValue: '', key: 'user_name', regexpFilter: '', value: '$.user_name'],
                    [defaultValue: '', key: 'project.git_ssh_url', regexpFilter: '', value: '$.project.git_ssh_url']
                ],
            ]

        def triggers = pageOption.util.pipelineGenericTriggers(triggerCfg)
        def ret = pageOption.getDefaultPageArgs()
        ret.add(triggers)
        return ret
    }
}
