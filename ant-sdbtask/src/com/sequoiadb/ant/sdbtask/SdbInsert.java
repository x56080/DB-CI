/**
 * 
 */
package com.sequoiadb.ant.sdbtask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sequoiadb.base.CollectionSpace;
import com.sequoiadb.base.DBCollection;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;


/**
 * @author qiushanggao
 *
 */
public class SdbInsert extends Task {
	
	private String uuid = null;
	private String CSName = null;
	private String CLName = null;
	private String record = null;
	private boolean failonerror = true;
	
	public void setSdbhandle(String value)
	{
		uuid = value;
	}
	public void setCsname(String value)
	{
		CSName = value;
	}
	public void setClname(String value)
	{
		CLName = value;
	}
	public void setRecord(String value)
	{
		record = value;
	}
	
	public void setFailonerror(String value)
	{
		failonerror = Boolean.parseBoolean(value);
	}
	
	public void execute()
	{
		Object obj = this.getProject().getReference(uuid);
		if (! (obj instanceof Sequoiadb))
		{
			throw new BuildException("The SdbUUID" + uuid + " cannot get Sequoiadb Object.");			
		}
		
		try
		{
			Sequoiadb sdb = (Sequoiadb) obj;
			CollectionSpace cs = sdb.getCollectionSpace(CSName);
			DBCollection cl= cs.getCollection(CLName);
			
			cl.insert(record);
		}
		catch(BaseException e)
		{
			if (failonerror)
			{
				throw new BuildException(e);
			}
			else
			{
				log("Failed to insert record. exception=" + e);
			}
		}
		
	}
}
