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
		try {

			ReplicaGroup group = null;
			
			try
			{
				setName(CATALOG_GROUP_NAME);
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
