/**
 * 
 */
package com.sequoiadb.ant.sdbtask;

import org.apache.tools.ant.Task;

import com.sequoiadb.base.Sequoiadb;

/**
 * @author qiushanggao
 * 
 */
public class SdbCloseConnection extends Task {
	
	private String uuid = null;
	
	public void setSdbUID(String value)
	{
		uuid = value;
	}
	
	public void execute() {
		Object obj = this.getProject().getReference(uuid);
		if (obj instanceof Sequoiadb)
		{
			Sequoiadb sdb = (Sequoiadb) obj;
			sdb.disconnect();
		}
		
	}
}
