package com.sequoiadb.ant.sdbtask;

import org.apache.tools.ant.Task;

import com.sequoiadb.base.*;
import com.sequoiadb.exception.BaseException;
public class clearEnvCS extends Task {
	private String hostName;
	private String csprefix;
	private int port = 50000;
	
	public void setHostName( String value )
	{
		this.hostName = value ;
	}
	public void setCsprefix( String value )
	{
		this.csprefix = value;
	}
	public void setPort( String value )
	{
		this.port = Integer.parseInt( value );
	}
	
	public void execute(){
		try{
			Sequoiadb sdb = new Sequoiadb( this.hostName , port ,"" , "");
			DBCursor cur = sdb.listCollectionSpaces();
			while( cur.hasNext() ){
				if( cur.getNext().get("Name").toString().contains(this.csprefix) ){
					sdb.dropCollectionSpace(cur.getCurrent().get("Name").toString() );
					//break;
				}
			}
		}catch( BaseException e ){
	    	System.out.println(e.getMessage());
	    }
	}
	
	
	

}
