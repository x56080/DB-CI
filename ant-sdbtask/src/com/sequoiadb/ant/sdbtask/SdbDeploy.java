package com.sequoiadb.ant.sdbtask;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sequoiadb.ant.datatype.*;
import com.sequoiadb.base.ReplicaGroup;
import com.sequoiadb.base.ReplicaNode;
import com.sequoiadb.base.Sequoiadb;

public class SdbDeploy extends Task {
	private String hostName;
	private String coordport;

	private List<DataNodeGroup> dataNodeGroups = new ArrayList<DataNodeGroup>();

	// private List<CatalogNode> catalogNodes = new ArrayList<CatalogNodes>();

	public void setHost(String value) {
		hostName = value;
	}

	public void setCoordport(String value) {
		coordport = value;
	}

	public DataNodeGroup createDatagroup() {
		DataNodeGroup group = new DataNodeGroup();
		dataNodeGroups.add(group);
		return group;
	}

	public void execute() {

		String connString = this.hostName + ":" + this.coordport;
		Sequoiadb sdb = new Sequoiadb(connString);

		try {
			for (DataNodeGroup groupInfo : dataNodeGroups) {

				ReplicaGroup group = sdb.getReplicaGroupByName(groupInfo
						.getName());
				if (group == null) {
					group = sdb.createReplicaGroup(groupInfo.getName());
				}

				for (DataNode nodeInfo : groupInfo.getDataNode()) {

					ReplicaNode node = group.getNode(this.hostName,
							nodeInfo.getBasePort());
					if (node == null) {
						group.createNode(this.hostName, nodeInfo.getBasePort(),
								nodeInfo.getDbpath(), nodeInfo.getConfigMap());
					} else {
						throw new BuildException("Node repeat: hostname="
								+ this.hostName + "servicename:"
								+ nodeInfo.getBasePort());
					}
				}
				
				group.start();
			}
		} catch (Exception e) {

			e.printStackTrace();

			throw new BuildException(e.toString());
		}
	}
}
