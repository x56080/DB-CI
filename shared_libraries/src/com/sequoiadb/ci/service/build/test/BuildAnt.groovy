package com.sequoiadb.ci.service.build.test

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.SelfScript
import hudson.AbortException

class BuildAnt extends SelfScript {

    private int retryCount = 3
    private int intervalSecond = 600
    public int failCount = 0
    public int passCount = 0
    public int skipCount = 0
    public int totalCount = 0

    BuildAnt(CommonUtil commonUtil, ConfigMgr configMgr) {
        super(commonUtil, configMgr)
    }

    def call(ReadyEnv readyEnv) {
        boolean ret = false
        def cfg = readyEnv.getTestPjtCfg()
        def ws = util.getEnv("WORKSPACE")
        Map<String, String> argsMap = cfg.get("ANT_ARGS") as Map
        argsMap.put("SDBBRANCH", util.getEnv("${ExtArgs.BRANCH}"))

        String testTypeStr = (cfg.get("TESTCASE_TYPE") as Map).get("defVal")
        argsMap.put("TESTCASE_TYPE", testTypeStr)

        StringBuilder cmd = new StringBuilder(". /home/sdbadmin/.bashrc;")
            .append("ant -file build.xml")
            .append(" -DWORKSPACE=$ws")
        argsMap.each { key, val -> cmd.append(" -D$key=$val") }
        cmd.append(" ${cfg.get('ANT_TARGET')}")

        String antDir = "${readyEnv.getCiCloneDir()}/src"
        util.dir(antDir, {
            util.retry(retryCount, {
                ret = util.sh(cmd.toString())
                if (ret) return ret
                util.sh("sleep $intervalSecond;")
                throw new Exception('ant build failure')
            })
        })

        return ret
    }

    def junit() {
        def junit = util.junit("report/**/*.xml")

        this.failCount = junit.getFailCount()
        this.passCount = junit.getPassCount()
        this.skipCount = junit.getSkipCount()
        this.totalCount = junit.getTotalCount()
    }


    def getState() {
        return this.failCount == 0
    }
}
