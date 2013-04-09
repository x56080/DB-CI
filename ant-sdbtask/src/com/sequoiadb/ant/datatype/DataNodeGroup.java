/**
 * 
 */
package com.sequoiadb.ant.datatype;


import org.apache.tools.ant.BuildException;

import com.sequoiadb.base.ReplicaGroup;
import com.sequoiadb.base.ReplicaNode;
import com.sequoiadb.base.Sequoiadb;

/**
 * @author qiushanggao
 * 
 */
public class DataNodeGroup extends NodeGroup {

	@Override
	public void start(Sequoiadb sdb) throws BuildException {
		try {
			ReplicaGroup group = sdb.getReplicaGroupByName(getName());

			if (group == null) {
				group = sdb.createReplicaGroup(getName());
			}

			for (Node nodeInfo : getNodeList()) {
				ReplicaNode node = group.getNode(nodeInfo.getHost(),
						nodeInfo.getBasePort());

				if (node == null) {
					group.createNode(nodeInfo.getHost(),
							nodeInfo.getBasePort(), nodeInfo.getDbpath(),
							nodeInfo.getConfigMap());
				} else {
					throw new BuildException("Node repeat: hostname="
							+ nodeInfo.getHost() + "servicename:"
							+ nodeInfo.getBasePort());
				}
			}

			group.start();

		} catch (Exception e) {

			e.printStackTrace();

			throw new BuildException(e.toString());
		}
	}

	

}
