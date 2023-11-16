package com.sequoiadb.ci.service.build.test

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.SelfScript
import hudson.AbortException

class CollectLogs extends SelfScript {


    CollectLogs(CommonUtil commonUtil, ConfigMgr configMgr) {
        super(commonUtil, configMgr)
    }

    def call(ReadyEnv readyEnv) {
        String name = util.getEnv("${ExtArgs.JOB_NAME}")
        String number = util.getEnv("${ExtArgs.BUILD_NUMBER}")
        String ws = "${this.getCiWorkspace()}/$name"

        String cmd = "ansible-playbook collectlog.yml -e tmp_local_path=$ws -e job_name=$name -e build_number=$number "
        util.dir(readyEnv.ansibleDir, { util.sh(cmd) })
    }

    def getStateByReport() {
        int testSuite = 0, testError = 0, testFailBegin = 0, testFailEnd = 0

        String reportDir = "${this.getJkWorkspace()}/report"
        if (!util.fileExists(reportDir)) new AbortException("report directory is not exist")

        util.dir(reportDir, {
            testFailBegin = Integer.valueOf(util.shWithReturnStdout('egrep "<failure>" ./*/*.xml  |wc -l ').toString())
            testFailEnd = Integer.valueOf(util.shWithReturnStdout('egrep "</failure>" ./*/*.xml |wc -l  ').toString())
            testError = Integer.valueOf(util.shWithReturnStdout('egrep "</error>" ./*/*.xml |wc -l  ').toString())
            testSuite = Integer.valueOf(util.shWithReturnStdout('egrep "<testsuite" ./*/*.xml |wc -l ').toString())
        })

        util.println("testsuitenum=$testSuite, testerrornum=$testFailBegin, testfailendnum=$testFailEnd, testfailbeginnum=$testFailEnd")
        return testError == 0 && testFailBegin == 0 && testFailEnd == 0
    }

}
