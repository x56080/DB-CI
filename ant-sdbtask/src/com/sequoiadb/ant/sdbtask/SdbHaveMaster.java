package com.sequoiadb.ant.sdbtask;


import org.apache.tools.ant.Task;
import org.bson.BSONObject;
import org.bson.types.BasicBSONList;
import com.sequoiadb.base.ReplicaGroup;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;

public class SdbHaveMaster extends Task {
	private String hostName = "localhost" ;
	private String port = "50000" ; 
	private String groupName = null ; 
	private String waitTime = "20" ; 
	private String propertyName= null ;
	
	public void setPropertyName( String value )
	{
		this.propertyName = value ;
	}
	public void setPort( String value )
	{
		this.port = value ; 
	}
	public void setHostName( String value )
	{
		this.hostName = value ; 
	}
	public void setGroupName( String value )
	{
		this.groupName = value ; 
	}
	public void setWaitTime( String value )
	{
		this.waitTime = value ; 
	}
	
	private boolean checkMaster()
	{
		Sequoiadb sdb = new Sequoiadb( this.hostName , Integer.parseInt( this.port ) , "" ,"") ; 
		ReplicaGroup RG = sdb.getReplicaGroup( this.groupName ) ;
		//GroupID  ;
		String groupID = RG.getDetail().get( "GroupID" ).toString() ;
		int nodeNum = RG.getNodeNum(null) ; 
		System.out.println( RG.getNodeNum(null) ) ;
		BasicBSONList bson_list = (BasicBSONList)RG.getDetail().get("Group") ;
		for(int i = 0 ; i < nodeNum ; i++ )
		{
			BSONObject oneBson = (BSONObject) bson_list.get( i ) ; 
			String nodeID = oneBson.get( "NodeID" ).toString() ;
			try{
			if( sdb.getSnapshot(7,"{GroupID:"
					+ groupID + ",NodeID:"
					+ nodeID + "}"
					, "{\"IsPrimary\":null}"
					, null).hasNext() )
			{
				String isMaster = sdb.getSnapshot(7,"{GroupID:"
							+ groupID + ",NodeID:"
							+ nodeID + "}"
							, "{\"IsPrimary\":null}"
							, null)
							.getNext().get("IsPrimary").toString() ;
				if( isMaster.equals( "true" ) )
					return true ;
			}
			}catch( BaseException e ){
				
			}
		}
	//	
		return false; 
	}
	
	public void execute ()
	{
		int times = Integer.parseInt( this.waitTime ) ;
		int i = 0 ;
		for(; i < times ; ++i )
		{
			if( false == checkMaster() )
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else
			{	
				this.getProject().setProperty( this.propertyName , "true" ) ;
				break ;
			}
		}
		if( ! (i < times) )
			this.getProject().setProperty( this.propertyName , "false" ) ;
	}

}
