package com.sequoiadb.ci.service.entry

class CompileType {
    protected String type;
    protected String arch;

    String getType() {
        return type
    }

    String getArch() {
        return arch
    }

    CompileType(String target) {
        def split = target.split("\\.")
        if (split.length >= 2) {
            this.type = split[0]
            this.arch = split[1]
        }
    }

    String getName() {
        return "${type}_${arch}"
    }
}
