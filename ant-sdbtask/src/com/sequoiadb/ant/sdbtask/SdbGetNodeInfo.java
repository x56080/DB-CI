package com.sequoiadb.ant.sdbtask;

import java.util.List;
import com.sequoiadb.base.*;
import org.apache.tools.ant.Task;
import com.sequoiadb.ant.tools.*;

import org.apache.tools.ant.types.Parameter;


public class SdbGetNodeInfo extends Task{
	private String hostName ; 
	//private String propertyHostName ;
	//private String propertyNodePort ; 
	private String groupName ; 
	private String getNodeType ; 
	private String getNum = "1" ; 
	private setPropertyInfo setProInfo = null ; 
	private hostNames htNames = null ; 
	

	public void setGetNum( String value )
	{
		this.getNum = value ; 
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
	
	public void createHostName()
	{
		this.htNames = new hostNames() ; 
	}
	
	public void createSetProperty()
	{
		this.setProInfo = new setPropertyInfo() ; 
	}
	
	public int setProperty( ReplicaGroup group )
	{
		String propertyHostName = group.getMaster().getHostName().toString() ;
		String propertyNodePort = Integer.toString( group.getMaster().getPort() ) ;
		List<sdbProperty> listPro = this.setProInfo.getListPro() ; 
		if( "master" == this.getNodeType || ( "slave" == this.getNodeType && this.getNum == "1" ) )
		{
			if( "master" != this.getNodeType )
			{
				propertyHostName = group.getSlave().getHostName().toString() ;
				propertyNodePort = Integer.toString( group.getSlave().getPort() ) ; 
			}
			for( sdbProperty sdbpro : listPro )
			{
				this.getProject().setProperty( sdbpro.getProName() , propertyNodePort ) ;
				this.getProject().setProperty( sdbpro.getProPort()  , propertyNodePort ) ;
			}
			
			return 0 ; 
		}
		if( "master" != this.getNodeType && this.getNum != "1" && this.htNames != null )
		{
			
			List<Parameter> listHtName = this.htNames.getListParameter() ; 
			for( Parameter p : listHtName )
			{
				if ( propertyHostName == p.getValue() )
				{
					listHtName.remove( p ) ; 
					break ; 
				}
			}
			int i = 0 ; 
			for( sdbProperty sdbpro : listPro )
			{
				this.getProject().setProperty( sdbpro.getProName() , listHtName.get(i++).getValue() ) ;
				this.getProject().setProperty( sdbpro.getProPort()  , propertyNodePort ) ;

			}
		}
		return 0 ; 
		
	}
	
	public void execute()
	{
		Sequoiadb sdb = new Sequoiadb( this.hostName  ,50000 , "" ,"") ;

		ReplicaGroup group = sdb.getReplicaGroup( this.groupName ) ;
		
		this.setProperty( group ) ; 

	}

	
}
