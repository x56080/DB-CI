/**
 * 
 */
package com.sequoiadb.ant.datatype;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.sequoiadb.base.ReplicaGroup;
import com.sequoiadb.base.ReplicaNode;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;

/**
 * @author qiushanggao
 *
 */
public abstract class NodeGroup {
	private String name;

	private List<Node> nodes = new ArrayList<Node>();
	
	public abstract void start(Sequoiadb sdb) throws BuildException;
	
	public void waitForStart(Sequoiadb sdb, long timeout) throws BuildException {
		
		ReplicaGroup group = sdb.getReplicaGroup(getName());
		
		//Wait for group select master, max wait time is 120sec;
		int i = 0;
		for(i = 0; i < timeout; i++)
		{
			try
			{
				ReplicaNode masterNode = group.getMaster();
				if (masterNode != null)
				{
					break;
				}
				
				Thread.sleep(1000);
			}
			catch(BaseException baseException)
			{
			}
			catch (InterruptedException e) {
			}
		}
		
		
		if (i >= timeout)
		{
			throw new BuildException("Group:" + this.getName() + " select master timeout.");
		}
		
	}
	
	
	public void setName(String value)
	{
		name = value;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Node createNode()
	{
		Node node = new Node();
		nodes.add(node);
		
		return node;
	}
	
	public List<Node> getNodeList()
	{
		return nodes;
	}
	
	
}
