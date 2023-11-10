package com.sequoiadb.ci.service.build.test

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.SelfScript

class BuildEnv extends SelfScript {

    private ReadyEnv readyEnv = null
    private String ansibleDir = null

    BuildEnv(CommonUtil commonUtil, ConfigMgr configMgr) {
        super(commonUtil, configMgr)
    }

    def reset(ReadyEnv readyEnv) {
        this.readyEnv = readyEnv
        this.ansibleDir = "${readyEnv.getPipCloneDir()}/ansible"
        genHostFile(ansibleDir)
        installDeploy(ansibleDir)
    }

    private def installDeploy(String ansibleDir) {
        boolean ret = false
        StringBuilder cmd = new StringBuilder("ansible-playbook resetenv.yml")
        String runpkg = util.shWithReturnStdout("basename ${readyEnv.getCopyRunDir()}/${readyEnv.getTestPjtCfg().get('RUNNAME')}")

        StringBuilder hostListStr = new StringBuilder("\"[")
        List<String> hostList = readyEnv.getTestPjtCfg().get("DEPLOY_NODE") as List
        for (final def item in hostList) hostListStr.append("'$item',")
        hostListStr.setLength(hostListStr.length() - 1)
        hostListStr.append("]\"")

        def testType = util.getEnv("${ExtArgs.TEST_PROJECT}")
        def confFile = mgr.get("TypeDeployConfMap/$testType")

        Map argsValMap = readyEnv.getTestPjtCfg().get("ANT_ARGS") as Map
        Map<String, String> args = [
            sdb_install_path           : argsValMap.get("INSTALL_DIR"),
            install_package_name       : runpkg,
            local_install_package_path : readyEnv.getCopyRunDir(),
            remote_install_package_path: argsValMap.get("CI_WORK_DIR"),
            deploy_metadata_dir        : argsValMap.get("METADATA_DIR"),
            conf_file                  : confFile,
            branch                     : util.getEnv("${ExtArgs.BRANCH}"),
            hostlist                   : hostListStr.toString(),
        ]
        args.each { key, val -> cmd.append(" -e $key=$val") }
        util.dir(ansibleDir, { ret = util.sh(cmd.toString()) })
        if (!ret) throw new Exception('install deploy failure')
        return ret
    }

    private def genHostFile(String ansibleDir) {
        StringBuilder hostListStr = new StringBuilder()
        List<String> hostList = readyEnv.getTestPjtCfg().get("DEPLOY_NODE") as List
        for (final def item in hostList) hostListStr.append("$item,")
        hostListStr.setLength(hostListStr.length() - 1)
        return util.sh("ansible-playbook $ansibleDir/genhostfile.yml -e output=$ansibleDir -e host_list=${hostListStr.toString()}")
    }

}
