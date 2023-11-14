properties = [:]
/**
 * 初始化
 */
properties.base_build = [:]
properties.commit_build = [:]
properties.daily_build = [:]
properties.release_build = [:]


/**
 * 支持选项
 */
properties.base_build.supportBranch = ['master', 'v3.4',]
properties.base_build.supportArch = ['x86', 'arm64']
properties.base_build.supportType = ['CONFIGURE', 'NORMAL_STANDALONE', 'NORMAL_G3D3', 'SYNC']


/**
 * 地址
 */
properties.base_build.url = [
    db_testcase: "http://gitlab.sequoiadb.com/sequoiadb/sequoiadb.git",
    db_compile : "http://gitlab.sequoiadb.com/sequoiadb/sequoiadb.git",
    db_ci      : "http://gitlab.sequoiadb.com/yaoyiming/db-ci.git",
    buildrun   : "http://gitlab.sequoiadb.com/sequoiadb/build_run.git"
]


/**
 * 构建保留
 */
properties.daily_build.buildDiscarder = ['artifactNumToKeepStr': '7', 'numToKeepStr': '14', 'daysToKeepStr': '14', 'artifactDaysToKeepStr': '3']
properties.commit_build.buildDiscarder = ['artifactNumToKeepStr': '3', 'numToKeepStr': '3', 'daysToKeepStr': '7', 'artifactDaysToKeepStr': '2']
properties.release_build.buildDiscarder = ['artifactNumToKeepStr': '1', 'numToKeepStr': '1', 'daysToKeepStr': '60', 'artifactDaysToKeepStr': '60']
properties.base_build.buildDiscarder = ['artifactNumToKeepStr': '3', 'numToKeepStr': '14', 'daysToKeepStr': '14', 'artifactDaysToKeepStr': '2']


/**
 * 外部必填参数
 */
properties.daily_build.outsideEnv = [testbuild: [required: ['BRANCH', 'TEST_PROJECT', 'TEST_ARCH',]], compilebuild: [required: ['BRANCH', 'COMPILE_TYPE',]]]
properties.release_build.outsideEnv = [testbuild: [required: ['BRANCH', 'TEST_PROJECT', 'TEST_ARCH',]], compilebuild: [required: ['BRANCH',]]]
properties.commit_build.outsideEnv = [testbuild: [required: ['BRANCH', 'TEST_PROJECT', 'TEST_ARCH',]], compilebuild: [required: ['BRANCH', 'COMPILE_TYPE',]]]
properties.base_build.outsideEnv = [testbuild: [required: ['BRANCH', 'TEST_PROJECT', 'TEST_ARCH',]], compilebuild: [required: ['BRANCH', 'COMPILE_TYPE',]]]


// workspace
properties.base_build.ciworkspace = '/hdd/ci/workspace'


//--------------------------------------------------------------------------------------------------
//- 测试配置部分

/**
 * ant -f build.xml 所需参数
 */
properties.base_build.ANT_ARGS = [:]
properties.base_build.ANT_ARGS.DEPLOY_MODE = 'G3D3'
properties.base_build.ANT_ARGS.NEED_INSTALL_DEPLOY = false
properties.base_build.ANT_ARGS.BACKUP_LOG_WHEN_FAIL = true
properties.base_build.ANT_ARGS.BREAK_ON_FAILURE = false
properties.base_build.ANT_ARGS.COVERAGE = false
properties.base_build.ANT_ARGS.ISMVCC = false
properties.base_build.ANT_ARGS.BUILD_NUMBER = "${BUILD_NUMBER}"

properties.base_build.ANT_ARGS.CI_WORK_DIR = "/hdd/ci/workspace/${JOB_NAME}"
properties.base_build.ANT_ARGS.INSTALL_DIR = "/hdd/ci/sequoiadb/${JOB_NAME}/sequoiadb"
properties.base_build.ANT_ARGS.METADATA_DIR = "/ssd/ci/workspace/${JOB_NAME}/sequoiadb/database"

properties.base_build.TypeDeployConfMap = [
    CONFIGURE        : "deploy_conf_config.js",
    NORMAL_STANDALONE: "deploy_conf_standalone.js",
    NORMAL_G3D3      : "deploy_conf_g3d3.js",
    SYNC             : "deploy_conf_g3d3.js",
]


//--------------------------------------------------------------------------------------------------
//- 编译配置部分

/**
 * 编译类型
 */
properties.base_build.supportCompileTypes = [
    "enterprise_debug.x86",
    "enterprise_debug.arm64",
    "enterprise_release.x86",
    "enterprise_release.arm64",
    "enterprise_debug_coverage.x86",
    "enterprise_debug_coverage.arm64",
    "community_release.x86",
]


properties.base_build.machine = [
    compilesdb: [arm64: 'compile_arm', x86: 'compile_x86',],
    buildrun  : [arm64: 'build_run', x86: 'build_run',],
    docs      : [arm64: 'compile_doc', x86: 'compile_doc',],
    calldocs  : [arm64: 'compile_db_doc', x86: 'compile_db_doc',],
]

/**
 * 编译部分的归档规则
 */
properties.release_build.archive = [
    buildrunArchives: ["release/*.run"],
    compileArchive  : ["release/sequoiadb/VERSION",
                       "release/**/sequoiadb.tar.gz",
                       "release/*.tar.gz",
                       "release/SequoiaDB_usermanuals_*",]
]
properties.base_build.archive = [
    buildrunArchives: ["release/*.run"],
    compileArchive  : ["release/**/sequoiadb.tar.gz",
                       "release/sequoiadb-driver-*.tar.gz",
                       "release/sequoiadb-*-bin.tar.gz",
                       "release/*.log",
                       "release/sequoiadb/VERSION",
                       "release/SequoiaDB_usermanuals_*",],
]

/**
 * Archive path
 */
properties.release_build.collectDir = "$WORKSPACE/archive/SequoiaDB/$BRANCH"
properties.release_build.saveDir = "/ssd/jenkins_archive/archive_new/SequoiaDB"
properties.release_build.archiveDir = "/ssd/jenkins_archive/publish_archive/版本归档_NEW/SequoiaDB/$BRANCH"

/**
 * 发布模式下文件归档位置配置
 */
properties.release_build.collectTarget = [
    'sequoiadb-$VERSION-linux_x86_64-bin.tar.gz'                      : "x86_64",
    'sequoiadb-$VERSION-linux_x86_64-enterprise-bin.tar.gz'           : "x86_64",
    'sequoiadb-$VERSION-linux_aarch64-enterprise-bin.tar.gz'          : "aarch64",
    'sequoiadb-$VERSION-linux_x86_64-enterprise-hybrid-bin.tar.gz'    : "x86_64",

    'sequoiadb-$VERSION-linux_x86_64-installer.run'                   : "x86_64",
    'sequoiadb-$VERSION-linux_x86_64-enterprise-installer.run'        : "x86_64",
    'sequoiadb-$VERSION-linux_x86_64-enterprise-hybrid-installer.run' : "x86_64",
    'sequoiadb-$VERSION-linux_aarch64-enterprise-installer.run'       : "aarch64",
    'sequoiadb-$VERSION-linux_aarch64-enterprise-hybrid-installer.run': "aarch64",

    'sequoiadb-driver-$VERSION-linux_x86_64.tar.gz'                   : 'x86_64/driver-$VERSION',
    'C#-$VERSION-linux_x86_64.tar.gz'                                 : 'x86_64/driver-$VERSION',
    'C&CPP-$VERSION-linux_x86_64.tar.gz'                              : 'x86_64/driver-$VERSION',
    'Hadoop-$VERSION-linux_x86_64.tar.gz'                             : 'x86_64/driver-$VERSION',
    'Flink-$VERSION-linux_x86_64.tar.gz'                              : 'x86_64/driver-$VERSION',
    'Java-$VERSION-linux_x86_64.tar.gz'                               : 'x86_64/driver-$VERSION',
    'PHP-$VERSION-linux_x86_64.tar.gz'                                : 'x86_64/driver-$VERSION',
    'Postgresql-$VERSION-linux_x86_64.tar.gz'                         : 'x86_64/driver-$VERSION',
    'Python-$VERSION-linux_x86_64.tar.gz'                             : 'x86_64/driver-$VERSION',
    'Spark-$VERSION-linux_x86_64.tar.gz'                              : 'x86_64/driver-$VERSION',

    'sequoiadb-driver-$VERSION-linux_aarch64.tar.gz'                  : 'aarch64/driver-$VERSION',
    'C#-$VERSION-linux_aarch64.tar.gz'                                : 'aarch64/driver-$VERSION',
    'C&CPP-$VERSION-linux_aarch64.tar.gz'                             : 'aarch64/driver-$VERSION',
    'Hadoop-$VERSION-linux_aarch64.tar.gz'                            : 'aarch64/driver-$VERSION',
    'Flink-$VERSION-linux_aarch64.tar.gz'                             : 'aarch64/driver-$VERSION',
    'Java-$VERSION-linux_aarch64.tar.gz'                              : 'aarch64/driver-$VERSION',
    'PHP-$VERSION-linux_aarch64.tar.gz'                               : 'aarch64/driver-$VERSION',
    'Postgresql-$VERSION-linux_aarch64.tar.gz'                        : 'aarch64/driver-$VERSION',
    'Python-$VERSION-linux_aarch64.tar.gz'                            : 'aarch64/driver-$VERSION',
    'Spark-$VERSION-linux_aarch64.tar.gz'                             : 'aarch64/driver-$VERSION',

    'SequoiaDB_usermanuals_$VERSION.chm'                              : '',
    'SequoiaDB_usermanuals_$VERSION.pdf'                              : '',
    'SequoiaDB_usermanuals_$VERSION.tar.gz'                           : '',

    "SequoiaDB_usermanuals_${BRANCH}.chm"                             : '',
    "SequoiaDB_usermanuals_${BRANCH}.pdf"                             : '',
    "SequoiaDB_usermanuals_${BRANCH}.tar.gz"                          : '',
]
return this


