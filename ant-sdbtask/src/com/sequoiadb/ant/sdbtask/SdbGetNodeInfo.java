package com.sequoiadb.ant.sdbtask;

import com.sequoiadb.base.*;
import org.apache.tools.ant.Task;

public class SdbGetNodeInfo extends Task{
	private String hostName ; 
	private String propertyHostName ;
	private String propertyNodePort ; 
	private String groupName ; 
	private String getNodeType ; 

	public void setPropertyHostName( String value )
	{
		this.propertyHostName = value ; 
	}
	public void setPropertyNodePort( String value )
	{
		this.propertyNodePort = value ; 
	}
	public void setHostName( String value )
	{
		this.hostName = value ; 
	}
	public void setGroupName( String value )
	{
		this.groupName = value ; 
	}
	public void setGetNodeType( String value )
	{
		this.getNodeType = value ; 
	}
	
	public void execute()
	{
		Sequoiadb sdb = new Sequoiadb( this.hostName  ,50000 , "" ,"") ;
		
		String propertyHostName = null ;
		String propertyNodePort = null ;
		ReplicaGroup group = sdb.getReplicaGroup( this.hostName ) ; 
		
		if( "master" == this.getNodeType )
		{
			propertyHostName = group.getMaster().getHostName().toString() ;
			propertyNodePort = Integer.toString( group.getMaster().getPort() ) ; 
		}else{
			propertyHostName = group.getSlave().getHostName().toString() ;
			propertyNodePort = Integer.toString( group.getSlave().getPort() ) ; 
		}
		//this.getProject().addReference(strUUID, sdb);
		this.getProject().setProperty( this.propertyHostName  , propertyHostName ) ;
		
		this.getProject().setProperty( this.propertyNodePort  , propertyNodePort ) ;
	}

	
}
