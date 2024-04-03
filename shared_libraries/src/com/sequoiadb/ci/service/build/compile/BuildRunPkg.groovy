package com.sequoiadb.ci.service.build.compile

import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.common.SubExtArgs
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.SelfScript

class BuildRunPkg extends SelfScript {

    private String wsDir = "sequoiadb_build_run"


    BuildRunPkg(CommonUtil commonUtil, ConfigMgr configMgr) {
        super(commonUtil, configMgr)
    }


    def build(String compileType, String arch) {
        Map compileArgs = [
            "tag"    : ["arm64": ["arch": "arm64"], "x86": ["arch": "x86_64"]],
            "default": [
                "tar"   : "sequoiadb.tar.gz",
                "giturl": mgr.get("url/db_compile"),
                "branch": util.getEnv("$ExtArgs.BRANCH"),
            ],
        ]


        Map argsMap = compileArgs.get('default')
        argsMap.putAll(compileArgs.tag.get(arch))

        String cmd = "bash callbuildpackage.sh "
        argsMap.each { key, val -> cmd += "--$key $val " }
        cmd += util.getEnvToBoolean("$SubExtArgs.SKIP_CHECK_DIRTREE") ? '-s' : ''

        boolean ret = false
        util.gitClone("${super.getJkWorkspace()}/$wsDir", mgr.get("url/buildrun") as String, 'master', false)

        String targetArtifacts = "release/${compileType}_${arch}/sequoiadb.tar.gz"
        def unarchiveMap = ["$targetArtifacts": "$wsDir/sequoiadb.tar.gz"]
        util.println(unarchiveMap.toMapString())
        util.unarchive(unarchiveMap)

        util.dir(wsDir, { ret = util.sh(cmd) })
        if (!ret) throw new Exception('compile buildrun failure')
    }


    def archive() {
        String archiveRule = ''
        List<String> list = configMgr.get('archive/buildrunArchives') as List
        for (final def item in list) archiveRule += "$item,"
        util.dir(wsDir, { util.archiveArtifacts(archiveRule) })
    }
}
