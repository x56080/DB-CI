/**
 * 
 */
package com.sequoiadb.ant.datatype;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qiushanggao
 *
 */
public class DataNodeGroup {
	private String name;

	private List<DataNode> dataNodes = new ArrayList<DataNode>();
	
	
	public void setName(String value)
	{
		name = value;
	}
	
	public String getName()
	{
		return name;
	}
	
	public DataNode createDataNode()
	{
		DataNode node = new DataNode();
		dataNodes.add(node);
		
		return node;
	}
	
	public List<DataNode> getDataNode()
	{
		return dataNodes;
	}
	
}
