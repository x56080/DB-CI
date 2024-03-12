properties = [:]
properties.base_build = [:]

/**
 * =================================================================================================
 *   CompileBuild CfgSet
 */
properties.base_build.machine = [compilesdb: [arm64: 'compile_arm', x86: 'compile_x86_1',]]
/**
 * Call child project name of test
 */
properties.base_build.testproject = [
    "test_master_sequoiadb_configure",
    "test_master_sequoiadb_sync",
    "test_master_sequoiadb_normal_g3d3",
    "test_master_sequoiadb_normal_standalone",
]




/**
 * =================================================================================================
 *  TestBuild CfgSet
 */

properties.base_build.x86 = [BASE: [:], CONFIGURE: [:], SYNC: [:], NORMAL_G3D3: [:], NORMAL_STANDALONE: [:]]
properties.base_build.arm64 = [BASE: [:], CONFIGURE: [:], SYNC: [:], NORMAL_G3D3: [:], NORMAL_STANDALONE: [:]]
properties.base_build.BRANCH = 'master'


/**
 * -------------------------------------------------------------------------------------------------
 *  x86 配置
 *  EXEC_NODE       调用ant执行节点
 *  DEPLOY_NODE     安装sdb节点
 *  ANT_TARGET      ant执行target
 *  ANT_ARGS ant    调用时传入的参数将于global中公共ant_args进行合并后传入
 *  TESTCASE_TYPE   用于设置页面multiChoice测试用例类型多选项
 *
 */
properties.base_build.x86.BASE.CI_BRANCH = 'v3.6_cgb'
properties.base_build.x86.BASE.DEPNAME = 'dailybuild_master_sequoiadb_x86'
properties.base_build.x86.BASE.RUNNAME = 'sequoiadb-*-linux_x86_64-enterprise-installer.run'
properties.base_build.x86.BASE.DEPNANE_COPY = "release/${properties.base_build.x86.BASE.RUNNAME}"


properties.base_build.x86.CONFIGURE.EXEC_NODE = 'test_sequoiadb_0'
properties.base_build.x86.CONFIGURE.DEPLOY_NODE = ['CI-A-24', 'CI-B-24', 'CI-C-24',]
properties.base_build.x86.CONFIGURE.ANT_ARGS = [ISMVCC: true,]
properties.base_build.x86.CONFIGURE.ANT_TARGET = 'configure_test_build'
properties.base_build.x86.CONFIGURE.TESTCASE_TYPE = [defVal: 'story_js,sdv_js,driver_php,sdv_java,driver_c,driver_cpp,driver_java,driver_python,thirdparty_fapmongo_fap3-java-mongo3,thirdparty_fapmongo,thirdparty_fapmongo_python,story_java',]

properties.base_build.x86.NORMAL_STANDALONE.EXEC_NODE = 'test_sequoiadb_1'
properties.base_build.x86.NORMAL_STANDALONE.DEPLOY_NODE = ['CI-B-50']
properties.base_build.x86.NORMAL_STANDALONE.ANT_TARGET = 'normal_test_build'
properties.base_build.x86.NORMAL_STANDALONE.ANT_ARGS = [DEPLOY_MODE: 'STANDALONE',]
properties.base_build.x86.NORMAL_STANDALONE.TESTCASE_TYPE = [defVal: 'story_js,story_java,sdv_js,sdv_java,driver_c,driver_cpp,driver_java,driver_python,driver_php',]


properties.base_build.x86.NORMAL_G3D3.EXEC_NODE = 'test_sequoiadb_2'
properties.base_build.x86.NORMAL_G3D3.DEPLOY_NODE = ['CI-A-21', 'CI-B-21', 'CI-C-21',]
properties.base_build.x86.NORMAL_G3D3.ANT_TARGET = 'normal_test_build'
properties.base_build.x86.NORMAL_G3D3.ANT_ARGS = [:]
properties.base_build.x86.NORMAL_G3D3.TESTCASE_TYPE = [defVal: 'story_js,sdv_js,driver_php,sdv_java,driver_c,driver_cpp,driver_java,driver_python,story_java',]


properties.base_build.x86.SYNC.EXEC_NODE = 'test_sequoiadb_3'
properties.base_build.x86.SYNC.DEPLOY_NODE = ['CI-A-23', 'CI-C-16', 'CI-C-23',]
properties.base_build.x86.SYNC.ANT_TARGET = 'sync_test_build'
properties.base_build.x86.SYNC.ANT_ARGS = [BREAK_ON_FAILURE: true]
properties.base_build.x86.SYNC.TESTCASE_TYPE = [defVal: 'tdd_mongoc,story_js,story_java,sdv_js,sdv_java,driver_c,driver_cpp,driver_java,driver_python,driver_php',]


/**
 * -------------------------------------------------------------------------------------------------
 *  arm 配置
 *
 */
properties.base_build.arm64.BASE.CI_BRANCH = 'v3.6_cgb'
properties.base_build.arm64.BASE.DEPNAME = 'dailybuild_master_sequoiadb_arm64'
properties.base_build.arm64.BASE.RUNNAME = 'sequoiadb-*-linux_aarch64-enterprise-installer.run'
properties.base_build.arm64.BASE.DEPNANE_COPY = "release/${properties.base_build.arm64.BASE.RUNNAME}"

properties.base_build.arm64.CONFIGURE.EXEC_NODE = 'test_sequoiadb_arm_1'
properties.base_build.arm64.CONFIGURE.DEPLOY_NODE = ['CI-X-26', 'CI-Y-26', 'CI-Z-26',]
properties.base_build.arm64.CONFIGURE.ANT_ARGS = [ISMVCC: true,]
properties.base_build.arm64.CONFIGURE.ANT_TARGET = 'configure_test_build'
properties.base_build.arm64.CONFIGURE.TESTCASE_TYPE = [defVal: 'story_js,sdv_js,driver_php,sdv_java,driver_java,driver_python,thirdparty_fapmongo_fap3-java-mongo3,thirdparty_fapmongo_python,story_java,driver_c,driver_cpp',]

properties.base_build.arm64.NORMAL_STANDALONE.EXEC_NODE = 'test_sequoiadb_arm_0'
properties.base_build.arm64.NORMAL_STANDALONE.DEPLOY_NODE = ['CI-Y-27']
properties.base_build.arm64.NORMAL_STANDALONE.ANT_TARGET = 'normal_test_build'
properties.base_build.arm64.NORMAL_STANDALONE.ANT_ARGS = [DEPLOY_MODE: 'STANDALONE',]
properties.base_build.arm64.NORMAL_STANDALONE.TESTCASE_TYPE = [defVal: 'story_js,story_java,sdv_js,sdv_java,driver_c,driver_cpp,driver_java,driver_python,driver_php',]

properties.base_build.arm64.NORMAL_G3D3.EXEC_NODE = 'test_sequoiadb_arm_1'
properties.base_build.arm64.NORMAL_G3D3.DEPLOY_NODE = ['CI-X-24', 'CI-Y-24', 'CI-Z-24',]
properties.base_build.arm64.NORMAL_G3D3.ANT_TARGET = 'normal_test_build'
properties.base_build.arm64.NORMAL_G3D3.ANT_ARGS = [:]
properties.base_build.arm64.NORMAL_G3D3.TESTCASE_TYPE = [defVal: 'story_js,sdv_js,driver_php,sdv_java,driver_c,driver_cpp,driver_java,driver_python,story_java',]


properties.base_build.arm64.SYNC.EXEC_NODE = 'test_sequoiadb_arm_0'
properties.base_build.arm64.SYNC.DEPLOY_NODE = ['CI-X-25', 'CI-Y-25', 'CI-Z-25',]
properties.base_build.arm64.SYNC.ANT_TARGET = 'sync_test_build'
properties.base_build.arm64.SYNC.ANT_ARGS = [BREAK_ON_FAILURE: true]
properties.base_build.arm64.SYNC.TESTCASE_TYPE = [defVal: 'story_js,story_java,sdv_js,sdv_java,driver_java,driver_python,driver_php,driver_c,driver_cpp',]


return this
