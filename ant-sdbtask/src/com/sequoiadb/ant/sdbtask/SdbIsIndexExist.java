package com.sequoiadb.ant.sdbtask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sequoiadb.base.CollectionSpace;
import com.sequoiadb.base.DBCollection;
import com.sequoiadb.base.DBCursor;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;

public class SdbIsIndexExist extends Task {
	private String uuid = null;
	
	private String clName = null;
	private String csName = null;
	private String indexName = null;
	
	public void setSdbhandle(String value)
	{
		uuid = value;
	}
	public void setClname(String value)
	{
		clName = value;
	}
	public void setCsname(String value)
	{
		csName = value;
	}
	public void setIndexname(String value)
	{
		indexName = value;
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
			CollectionSpace space = sdb.getCollectionSpace(csName);
			DBCollection cl = space.getCollection(clName);
			
			DBCursor cursor = null;
			if (indexName != null)
			{
				cursor = cl.getIndex(indexName);
			}
			else
			{
				cursor = cl.getIndexes();
			}
			
			if  (cursor == null ||  !cursor.hasNext())
			{
				throw new BuildException("Index:" + indexName + " is not exist in " + csName + "." + clName);
			}
			
			cursor.close();
		}
		catch(BaseException e)
		{
			throw new BuildException(e.toString());
		}
	}

}
