/**
 * 
 */
package com.sequoiadb.ant.datatype;

import org.apache.tools.ant.BuildException;

import com.sequoiadb.base.ReplicaGroup;
import com.sequoiadb.base.ReplicaNode;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;

/**
 * @author qiushanggao
 * 
 */
public class CataNodeGroup extends NodeGroup {

	private static String CATALOG_GROUP_NAME = "SYSCatalogGroup";

	@Override
	public void start(Sequoiadb sdb) throws BuildException {

		ReplicaGroup group = null;
		try {
			setName(CATALOG_GROUP_NAME);
			group = sdb.getReplicaGroup(getName());
		} catch (BaseException e) {
			group = null;
		}

		if (group == null) {
			// group is not exist
			Node nodeInfo = getNodeList().get(0);
			getNodeList().remove(0);

			try {
				// create group
				sdb.createReplicaCataGroup(nodeInfo.getHost(),
						nodeInfo.getBasePort(), nodeInfo.getDbpath(),
						nodeInfo.getConfigMap());
			} catch (BaseException e) {
				// Do nothing
			}
		}

	}

	@Override
	public void waitForStart(Sequoiadb sdb, long timeout) throws BuildException {

		ReplicaGroup group = null;

		// Wait for cata select group.
		int i = 0;
		while (true) {
			try {
				group = sdb.getReplicaGroup(getName());
				if (group != null) {
					break;
				}
			} catch (BaseException baseException) {
				// Do nothing
			}

			i++;
			if (i > timeout) {
				throw new BuildException("Group:" + this.getName()
						+ " select master timeout.");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		ReplicaNode node = null;
		for (Node nodeInfo : getNodeList()) {
			node = group.getNode(nodeInfo.getHost(),
					nodeInfo.getBasePort());

			if (node == null) {
				node = group.createNode(nodeInfo.getHost(),
						nodeInfo.getBasePort(), nodeInfo.getDbpath(),
						nodeInfo.getConfigMap());

				node.start();
			} else {
				throw new BuildException("Node repeat: hostname="
						+ nodeInfo.getHost() + "servicename:"
						+ nodeInfo.getBasePort());
			}
		}

		// Wait for cata select group.
		i = 0;
		while (true) {
			try {
				node = group.getMaster();
				if (group != null) {
					break;
				}
			} catch (BaseException baseException) {
				// Do nothing
			}

			i++;
			if (i > timeout) {
				throw new BuildException("Group:" + this.getName()
						+ " select master timeout.");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		//Wait selected master complete.
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		
	}
}
