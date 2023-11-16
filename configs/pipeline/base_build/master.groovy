properties = [:]

/**
 * =================================================================================================
 *   compilebuild 配置项目
 *
 */
properties.machine = [compilesdb: [arm64: 'compile_arm', x86: 'compile_x86_1',]]


/**
 * =================================================================================================
 *  testbuild 配置项目
 *
 */
properties.x86 = [BASE: [:], CONFIGURE: [:], SYNC: [:], NORMAL_G3D3: [:], NORMAL_STANDALONE: [:]]
properties.arm64 = [BASE: [:], CONFIGURE: [:], SYNC: [:], NORMAL_G3D3: [:], NORMAL_STANDALONE: [:]]
properties.BRANCH = 'master'


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
properties.x86.BASE.CI_BRANCH = 'v3.6cgb_new_arm_modify_03'
properties.x86.BASE.DEPNAME = 'dailybuild_master_sequoiadb_x86'
properties.x86.BASE.RUNNAME = 'sequoiadb-*-linux_x86_64-enterprise-installer.run'
properties.x86.BASE.DEPNANE_COPY = "release/${properties.x86.BASE.RUNNAME}"


properties.x86.CONFIGURE.EXEC_NODE = 'test_sequoiadb_0'
properties.x86.CONFIGURE.DEPLOY_NODE = ['CI-A-24', 'CI-B-24', 'CI-C-24',]
properties.x86.CONFIGURE.ANT_ARGS = [ISMVCC: true,]
properties.x86.CONFIGURE.ANT_TARGET = 'configure_test_build'
properties.x86.CONFIGURE.TESTCASE_TYPE = [defVal: 'story_js,sdv_js,driver_php,sdv_java,driver_c,driver_cpp,driver_java,driver_python,thirdparty_fapmongo_fap3-java-mongo3,thirdparty_fapmongo,thirdparty_fapmongo_python,story_java',]



properties.x86.NORMAL_STANDALONE.EXEC_NODE = 'test_sequoiadb_0'
properties.x86.NORMAL_STANDALONE.DEPLOY_NODE = ['CI-A-22']
properties.x86.NORMAL_STANDALONE.ANT_TARGET = 'normal_test_build'
properties.x86.NORMAL_STANDALONE.ANT_ARGS = [DEPLOY_MODE: 'STANDALONE',]
properties.x86.NORMAL_STANDALONE.TESTCASE_TYPE = [defVal: 'story_js,story_java,sdv_js,sdv_java,driver_c,driver_cpp,driver_java,driver_python,driver_php', ]
//properties.x86.NORMAL_STANDALONE.TESTCASE_TYPE = [defVal: 'driver_python',]


properties.x86.NORMAL_G3D3.EXEC_NODE = 'test_sequoiadb_2'
properties.x86.NORMAL_G3D3.DEPLOY_NODE = ['CI-A-21', 'CI-B-21', 'CI-C-21',]
properties.x86.NORMAL_G3D3.ANT_TARGET = 'normal_test_build'
properties.x86.NORMAL_G3D3.ANT_ARGS = [:]
properties.x86.NORMAL_G3D3.TESTCASE_TYPE = [defVal: 'story_js,sdv_js,driver_php,sdv_java,driver_c,driver_cpp,driver_java,driver_python,story_java',]


properties.x86.SYNC.EXEC_NODE = 'test_sequoiadb_3'
properties.x86.SYNC.DEPLOY_NODE = ['CI-A-23', 'CI-C-16', 'CI-C-23',]
properties.x86.SYNC.ANT_TARGET = 'sync_test_build'
properties.x86.SYNC.ANT_ARGS = [BREAK_ON_FAILURE: true]
properties.x86.SYNC.TESTCASE_TYPE = [defVal: 'tdd_mongoc,story_js,story_java,sdv_js,sdv_java,driver_c,driver_cpp,driver_java,driver_python,driver_php', ]


/**
 * -------------------------------------------------------------------------------------------------
 *  arm 配置
 *
 */
properties.arm64.BASE.CI_BRANCH = 'v3.6cgb_new_arm_modify_03'
properties.arm64.BASE.DEPNAME = 'dailybuild_master_sequoiadb_arm64'
properties.arm64.BASE.RUNNAME = 'sequoiadb-*-linux_aarch64-enterprise-installer.run'
properties.arm64.BASE.DEPNANE_COPY = "release/${properties.arm64.BASE.RUNNAME}"

properties.arm64.CONFIGURE.EXEC_NODE = 'test_sequoiadb_arm_1'
properties.arm64.CONFIGURE.DEPLOY_NODE = ['CI-X-26', 'CI-Y-26', 'CI-Z-26',]
properties.arm64.CONFIGURE.ANT_ARGS = [ISMVCC: true,]
properties.arm64.CONFIGURE.ANT_TARGET = 'configure_test_build'
properties.arm64.CONFIGURE.TESTCASE_TYPE = [defVal: 'story_js,sdv_js,driver_php,sdv_java,driver_java,driver_python,thirdparty_fapmongo_fap3-java-mongo3,thirdparty_fapmongo_python,story_java,driver_c,driver_cpp',]

properties.arm64.NORMAL_STANDALONE.EXEC_NODE = 'test_sequoiadb_arm_0'
properties.arm64.NORMAL_STANDALONE.DEPLOY_NODE = ['CI-Y-27']
properties.arm64.NORMAL_STANDALONE.ANT_TARGET = 'normal_test_build'
properties.arm64.NORMAL_STANDALONE.ANT_ARGS = [DEPLOY_MODE: 'STANDALONE',]
properties.arm64.NORMAL_STANDALONE.TESTCASE_TYPE = [defVal: 'story_js,story_java,sdv_js,sdv_java,driver_c,driver_cpp,driver_java,driver_python,driver_php',]

properties.arm64.NORMAL_G3D3.EXEC_NODE = 'test_sequoiadb_arm_1'
properties.arm64.NORMAL_G3D3.DEPLOY_NODE = ['CI-X-24', 'CI-Y-24', 'CI-Z-24',]
properties.arm64.NORMAL_G3D3.ANT_TARGET = 'normal_test_build'
properties.arm64.NORMAL_G3D3.ANT_ARGS = [:]
properties.arm64.NORMAL_G3D3.TESTCASE_TYPE = [defVal: 'story_js,sdv_js,driver_php,sdv_java,driver_c,driver_cpp,driver_java,driver_python,story_java',]


properties.arm64.SYNC.EXEC_NODE = 'test_sequoiadb_arm_0'
properties.arm64.SYNC.DEPLOY_NODE = ['CI-X-25', 'CI-Y-25', 'CI-Z-25',]
properties.arm64.SYNC.ANT_TARGET = 'sync_test_build'
properties.arm64.SYNC.ANT_ARGS = [BREAK_ON_FAILURE: true]
properties.arm64.SYNC.TESTCASE_TYPE = [defVal: 'story_js,story_java,sdv_js,sdv_java,driver_java,driver_python,driver_php,driver_c,driver_cpp',]


return this