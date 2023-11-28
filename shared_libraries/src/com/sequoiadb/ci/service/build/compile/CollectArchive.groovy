package com.sequoiadb.ci.service.build.compile

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.Impl.SubExtArgs
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import hudson.AbortException

class CollectArchive {

    private CommonUtil util = null
    private ConfigMgr mgr = null

    private String collectDir
    private String saveDir
    private String archiveDir
    private String scriptDir
    private String archivePath
    private String version

    CollectArchive(CommonUtil commonUtil, ConfigMgr configMgr) {
        this.util = commonUtil
        this.mgr = configMgr
    }

    def init() {
        saveDir = mgr.get("saveDir")
        collectDir = mgr.get("collectDir")
        archiveDir = mgr.get("archiveDir")
        archivePath = getJobBuildPath() + '/archive/release'
        version = getSdbVersion(archivePath)
        scriptDir = "${util.getEnv(ExtArgs.WORKSPACE.toString())}/script"
    }

    def call() {
        if (version == null || version == 'null' || version.toString().isEmpty()) {
            throw new AbortException("cannot read version info in CollectArchive")
        }

        util.println("build version: $version")
        util.clearDir(collectDir)
        filterCollect()

        util.clearDir(saveDir)
        util.copy2(collectDir, saveDir)
    }

    def archive() {
        util.dir(scriptDir, {
            util.sh("chmod 744 *.sh")
            util.sh("expect chroot.sh $collectDir $archiveDir")
        })
    }

    def filterCollect() {
        String branch = util.getEnv("$ExtArgs.BRANCH")

        def flag = []
        def map = mgr.get("collectTarget") as Map
        map.each { key, val ->
            key = key.toString().replace('$VERSION', version)
            val = val.toString().replace('$VERSION', version)

            def src = "$archivePath/$key"
            def dst = "$collectDir/$version/$val"
            if (util.fileExists(src)) {
                def ret = util.copy2(src, dst)
                if (key.contains('.run') && ret) tarRun(src, dst)
                flag.add(key)
            }
        }
        def unCopyStr = 'skip copy, cause no exist file [\n'
        map.each { key, val ->
            key = key.toString().replace('$VERSION', version)

            if (!flag.contains(key)) {
                if (key.toString().contains('hybrid') && branch != 'v3.4') return true
                unCopyStr += "$key,\n"
            }
        }
        util.println "$unCopyStr]"
    }

    def getJobBuildPath() {
        def jobNumber = util.getEnv("$ExtArgs.BUILD_NUMBER")
        def jobName = util.getEnv("$ExtArgs.JOB_NAME")
        def home = util.getEnv("$ExtArgs.JENKINS_HOME")
        return "$home/jobs/$jobName/builds/$jobNumber"
    }

    def getSdbVersion(String path) {
        return util.shWithReturnStdout("cat $path/*/VERSION |grep -i 'sequoiadb version' | awk 'NR==1{print \$NF}'")
    }

    def tarRun(String src, String dst) {
        util.sh("name=\$(basename $src .run) && target=\$(basename $src) && tar -C $dst -zcvf  $dst/\$name.tar.gz \$target")
    }

}
