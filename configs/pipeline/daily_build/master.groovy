properties = [:]
/**
 * Call child project name of test
 */
properties.testproject = [
    "test_master_sequoiadb_configure",
    "test_master_sequoiadb_sync",
    "test_master_sequoiadb_normal_g3d3",
    "test_master_sequoiadb_normal_standalone",
]

/**
 * Trigger cron
 */
properties.triggerCron = 'H 1 * * *'

/**
 * Machine node label
 */
properties.machine = [compilesdb: [arm64: 'compile_arm', x86: 'compile_x86_1',]]

return this