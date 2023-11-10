package com.sequoiadb.ci.service.build.compile


import com.sequoiadb.ci.common.ExtArgs
import com.sequoiadb.ci.utils.CommonUtil
import com.sequoiadb.ci.utils.ConfigMgr
import com.sequoiadb.ci.utils.SelfScript

class CompileSdb extends SelfScript {

    private String wsDirPrefix = "sequoiadb_compile"
    private String wsDir = null
    private String releasePrefix = "release"
    private String releaseDir = null
    private String wsDocDir = null


    CompileSdb(CommonUtil commonUtil, ConfigMgr configMgr) {
        super(commonUtil, configMgr)
    }


    def init() {
        releaseDir = "${this.getJkWorkspace()}/$releasePrefix"
        wsDir = "${this.getJkWorkspace()}/$wsDirPrefix"
    }

    def initDoc() {
        wsDocDir = "e:/ci/code/${util.getEnv(ExtArgs.BRANCH.toString())}/sequoiadb"
    }


    def compileTar(String compileType, String arch) {
        def args = [
            "default": [
                "install-dir": releaseDir,
                "logfile"    : "$releaseDir/build.log",
                "job"        : util.getEnv(ExtArgs.JOB_NUMBER.toString(), '6'),
            ],
            "tag"    : [
                "community_release"        : [:],
                "enterprise_debug"         : ["enterprise": "", "dd": ""],
                "enterprise_debug_coverage": ["enterprise": "", "dd": "", "coverage": ""],
                "enterprise_release"       : ["enterprise": ""],
                "enterprise_release_hybrid": ["enterprise": "", "hybrid": ""],
            ]
        ]
        compile(compileType, arch, args)
    }


    def compileDoc(String compileType, String arch) {
        def args = [
            "default": [
                "install-dir"           : releaseDir,
                "logfile"               : "$releaseDir/build-doc.log",
                "enable-windows-compile": "",
                "db-path"               : wsDocDir,
            ],
        ]
        compile(compileType, arch, args)
    }


    def cloneDoc() {
        def extensions = [
            [$class: 'RelativeTargetDirectory', relativeTargetDir: wsDocDir],
            [$class: 'CloneOption', timeout: 600, shallow: true, depth: 1],
            [$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [
                [path: 'client/'],
                [path: 'conf/'],
                [path: 'doc/'],
                [path: 'driver/'],
                [path: 'java/jdk_win32/'],
                [path: 'misc/'],
                [path: 'script/'],
                [path: 'SequoiaDB/'],
                [path: 'site_scons/'],
                [path: 'thirdparty/'],
                [path: 'tools/'],
                [path: 'SConstruct'],
                [path: 'build.py'],
                [path: 'gitbuild'],]
            ]]

        String url = mgr.get('url/db_compile')
        String branch = util.getEnv("$ExtArgs.GIT_SHA", util.getEnv("${ExtArgs.BRANCH}"))
        util.gitClone(wsDocDir, url, branch, false, extensions)
    }


    private def compile(String compileType, String arch, Map<String, Map<String, Map>> compileArgs) {
        def argsMap = compileArgs.default
        def tagMap = compileArgs.tag
        argsMap.putAll(tagMap == null ? [:] : tagMap.get(compileType))

        String cmd = ". ~/.bashrc && python build.py "
        argsMap.each { key, val -> cmd += "--$key $val " }

        boolean ret = false
        util.clearDir(releaseDir)
        util.mkdir(releaseDir)
        this.gitClone(wsDirPrefix, 'db_compile', false)
//        this.gitClone(wsDirPrefix, 'db_compile', true)
        util.dir(wsDir, {
            ret = util.sh(cmd)
//            ret = ret ? util.sh("git clean -fxd") : false
        })
        if (!ret) throw new Exception('compile failure')

        String src = "$releaseDir/sequoiadb.tar.gz"
        String releaseDirByType = util.isEnvAttrEmpty("$ExtArgs.COMPILE_TYPE") ?
            "$releaseDir/${compileType}_${arch}" :
            "$releaseDir/${util.getEnv("$ExtArgs.COMPILE_TYPE").replace(".","_")}"

        util.move(src, releaseDirByType)
    }


    def archive() {
        String archiveRule = ''
        List<String> list = configMgr.get('archive/compileArchive') as List
        for (final def item in list) archiveRule += "$item,"
        util.archiveArtifacts(archiveRule)
    }
}
