package com.sequoiadb.ci.service.build.test

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.SelfScript
import hudson.AbortException

class ReadyEnv extends SelfScript {

    protected Map testPjtCfg = [:]
    protected String pipCloneDir = null
    protected String ansibleDir = null
    protected String copyRunDir = null
    protected String ciCloneDir = null

    private String testPjt = null
    private String sdbCloneDir = null

    Map getTestPjtCfg() {
        return testPjtCfg
    }

    String getPipCloneDir() {
        return pipCloneDir
    }

    String getCopyRunDir() {
        return copyRunDir
    }

    String getCiCloneDir() {
        return ciCloneDir
    }

    String getAnsibleDir() {
        return ansibleDir
    }

    ReadyEnv(CommonUtil commonUtil, ConfigMgr configMgr) {
        super(commonUtil, configMgr)
    }

    def unlock() {
        String flag = (testPjtCfg.get("DEPLOY_NODE") as List<String>)[0].toLowerCase()
        boolean ret = util.sh("cd $pipCloneDir/script/; bash -x checkBuildEnv.sh -m unlock -f $flag")
        //boolean ret=util.sh("rm -rf /tmp/${flag}; rm -rf /tmp/${flag}.lock")
        if (!ret) throw new Exception('unlock failure')
    }

    def lock() {
        String buildNum = util.getEnv("$ExtArgs.BUILD_NUMBER")
        String jobName = util.getEnv("$ExtArgs.JOB_NAME")
        String flag = (testPjtCfg.get("DEPLOY_NODE") as List<String>)[0].toLowerCase()
        boolean ret = util.sh("bash -x $pipCloneDir/script/checkBuildEnv.sh -m lock -f $flag -b $buildNum -j $jobName")
        if (!ret) throw new Exception('lock failure')
    }

    def copyArchive() {
        String filter = mgr.get("DEPNANE_COPY")
        String dependent = commonUtil.isEnvAttrEmpty(ExtArgs.DEPNAME.toString()) ?
            testPjtCfg.get(ExtArgs.DEPNAME.toString()) :
            commonUtil.getEnv(ExtArgs.DEPNAME.toString())
        String dependentBuildNum = commonUtil.isEnvAttrEmpty("SDB_BUILD_NUMBER") ?
            null :
            commonUtil.getEnv("SDB_BUILD_NUMBER")

        String target = "./archive/current"
        copyRunDir = "${super.getJkWorkspace()}/$target"
        util.clearDir(copyRunDir)
        util.copyArtifacts(filter, dependent, target, dependentBuildNum)
    }

    def init(){
        pipCloneDir = "${super.getJkWorkspace()}/pipeline"
        sdbCloneDir = "${super.getJkWorkspace()}/sequoiadb"
        ciCloneDir = "${super.getJkWorkspace()}/sequoiadb/misc/ci"
        ansibleDir = "$pipCloneDir/ansible"
    }

    def readyScript() {
        String dbCiUrl = mgr.get("url/db_ci")
        String ciBranch = commonUtil.isEnvAttrEmpty(ExtArgs.CI_BRANCH.toString()) ?
            testPjtCfg.get(ExtArgs.CI_BRANCH.toString()) :
            commonUtil.getEnv(ExtArgs.CI_BRANCH.toString())
        this.gitClone("sequoiadb", "db_testcase")
        util.gitClone(ciCloneDir, dbCiUrl, ciBranch, true)
        util.dir(pipCloneDir, { util.checkoutScm() })

        String src = "$sdbCloneDir/testcase_new/*"
        String dest = "$sdbCloneDir/testcase"
        util.clearDir(dest)
        util.dir(dest, { util.sh("cp -r $src $dest") })
    }

    def node(def closure, TEST_PROJECT testProject = null) {
        this.testPjt = testProject == null ? util.getEnv("$ExtArgs.TEST_PROJECT") : testProject.toString()
        if (testPjtCfg.isEmpty()) initCfg()
        def label = testPjtCfg.get("EXEC_NODE") as String
        util.node2(label, closure)
    }

    private def initCfg() {
        def arch = util.getEnv("$ExtArgs.TEST_ARCH")
        def typePjt = "$arch/$testPjt"
        def testBaseProjectCfg = mgr.get("$arch/BASE") as Map
        def testProjectCfg = mgr.get(typePjt) as Map
        testProjectCfg = ConfigMgr.mergeMap(testBaseProjectCfg, testProjectCfg)
        if (testProjectCfg == null) {
            throw new AbortException("TEST_PROJECT is null of branch config file")
        }
        // 合并公共的ant_args
        def testAntPubCfg = mgr.get("ANT_ARGS") as Map
        def testAntBraCfg = (testProjectCfg as Map).get("ANT_ARGS") as Map
        def testAntCfg = ConfigMgr.mergeMap(testAntPubCfg, testAntBraCfg)
        (testProjectCfg as Map).put("ANT_ARGS", testAntCfg)

        testPjtCfg.putAll(testProjectCfg as Map)
        util.println("$typePjt CFG: $testPjtCfg")
    }

    enum TEST_PROJECT {
        CONFIGURE, NORMAL_STANDALONE, NORMAL_G3D3, SYNC
    }
}
