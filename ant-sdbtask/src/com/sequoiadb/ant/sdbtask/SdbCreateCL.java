package com.sequoiadb.ant.sdbtask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sequoiadb.base.CollectionSpace;
import com.sequoiadb.base.DBCollection;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;


public class SdbCreateCL extends Task {
	private String uuid = null;
	private String csName = null;
	private String clName = null;
	
	
	public void setSdbhandle(String value)
	{
		uuid = value;
	}
	
	public void setCsname(String value)
	{
		csName = value;
	}
	
	public void setClname(String value)
	{
		clName = value;
	}
	
	public void execute() {
		Object obj = this.getProject().getReference(uuid);
		if (! (obj instanceof Sequoiadb))
		{
			throw new BuildException("The SdbUUID" + uuid + " cannot get Sequoiadb Object.");			
		}
		
		try
		{
			Sequoiadb sdb = (Sequoiadb) obj;
			CollectionSpace space = sdb.getCollectionSpace(csName);
			space.createCollection(clName);
		}
		catch(BaseException e)
		{
			throw new BuildException(e.toString());
		}
	}

}
