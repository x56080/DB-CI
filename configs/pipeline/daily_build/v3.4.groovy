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
 * Trigger cron
 */
properties.triggerCron = 'H 0 * * *'


properties.supportCompileTypes = [
    "enterprise_debug.x86",
    "enterprise_debug.arm64",
    "enterprise_debug_coverage.x86",
    "enterprise_debug_coverage.arm64",
    "enterprise_release.x86",
    "enterprise_release.arm64",
    "enterprise_release_hybrid.x86",
    "enterprise_release_hybrid.arm64",
    "community_release.x86",
]
return this
