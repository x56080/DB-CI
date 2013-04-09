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
public class CataNodeGroup extends NodeGroup {

	@Override
	public void start(Sequoiadb sdb) throws BuildException {
		try {

			ReplicaGroup group = null;
			
			try
			{
				group = sdb.getReplicaGroup(getName());
			}
			catch(BaseException e)
			{
				group = null;
			}

			for (Node nodeInfo : getNodeList()) {
				
				if (group == null)
				{
					group = sdb.createReplicaCataGroup(nodeInfo.getHost(), nodeInfo.getBasePort(), nodeInfo.getDbpath(), nodeInfo.getConfigMap());
				}
				else
				{
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
			}
			group.start();

		} catch (Exception e) {

			e.printStackTrace();

			throw new BuildException(e.toString());
		}
		
	}
}
