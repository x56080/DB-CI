package com.sequoiadb.ant.sdbtask;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sequoiadb.ant.datatype.*;
import com.sequoiadb.base.ReplicaGroup;
import com.sequoiadb.base.ReplicaNode;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;

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

				ReplicaGroup group = sdb.getReplicaGroupByName(groupInfo.getName());
				
				
				if (group == null) {
					group = sdb.createReplicaGroup(groupInfo.getName());
					
					this.log("Create group:" + groupInfo.getName());
				}

				for (DataNode nodeInfo : groupInfo.getDataNode()) {

					ReplicaNode node = group.getNode(this.hostName,
							nodeInfo.getBasePort());
					if (node == null) {
						group.createNode(this.hostName, nodeInfo.getBasePort(),
								nodeInfo.getDbpath(), nodeInfo.getConfigMap());
						
						this.log("Create node host:" + this.hostName + ", port:" + nodeInfo.getBasePort());
					} else {
						throw new BuildException("Node repeat: hostname="
								+ this.hostName + "servicename:"
								+ nodeInfo.getBasePort());
					}
				}
				
				group.start();
			}
			
			for (DataNodeGroup groupInfo : dataNodeGroups)
			{
				ReplicaGroup group = sdb.getReplicaGroupByName(groupInfo
						.getName());
				
				//Wait for group select master, max wait time is 120sec;
				int i = 0;
				for(i = 0; i < 120; i++)
				{
					try
					{
						ReplicaNode masterNode = group.getMaster();
						if (masterNode != null)
						{
							break;
						}
					}
					catch(BaseException baseException)
					{
					}
					
					this.log("Wait group:" + groupInfo.getName() + " select master...");
					Thread.sleep(1000);
				}
				if (i >= 120)
				{
					throw new BuildException("Error: Group " + groupInfo.getName() + " select master time out.");
				}
				
				this.log("group:" + groupInfo.getName() + " select master compeleted.");
			}
			
		} catch (Exception e) {

			e.printStackTrace();

			throw new BuildException(e.toString());
		}
	}
}
