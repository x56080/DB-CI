properties = [:]
/**
 * Call child project name of test
 */
properties.testproject = [
    "test_3.4_sequoiadb_configure",
    "test_3.4_sequoiadb_sync",
    "test_3.4_sequoiadb_normal_g3d3",
    "test_3.4_sequoiadb_normal_standalone",
]

/**
 * Branch compile type
 */
properties.callBuildSdbType = [
    "enterprise_release.x86",
    "enterprise_release_hybrid.x86",
    "community_release.x86",
    "enterprise_release.arm64",
    "enterprise_release_hybrid.arm64"
]

return this
