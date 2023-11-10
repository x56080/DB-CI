package com.sequoiadb.ci.service.build.test

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.SelfScript

class BuildAnt extends SelfScript {

    private int retryNum = 3

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
            for (int i = 0; i < retryNum; i++) {
                int j = i + 1
                ret = util.sh(cmd.toString())
                if (!ret) {
                    util.println("== retry call $j ,because call ant build failure ==")
                    util.sh('list=$(ss -tunp |grep 22);echo "raw num: $(echo "$list"|wc -l)"')
                    if (j < retryNum) util.sh("sleep 1600; ss -tun;")
                    continue
                }
                break
            }
        })

        if (!ret) throw new Exception('ant build failure')
        return ret
    }

    def junit() {
        util.junit("report/**/*.xml")
    }
}
