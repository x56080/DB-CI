package com.sequoiadb.ant.sdbtask;


import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.InputStreamReader ;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;

public class SdbKillMasterNode extends Task {
	
	private String uuid = null;
	private String rgName = null;
	private boolean failonerror = true;
	private Process pro;

	public void setSdbhandle(String value)
	{
		uuid = value;
	}
	
	public void setRgname(String value)
	{
		rgName = value;
	}
	
	public void setFailonerror(String value)
	{
		failonerror = Boolean.parseBoolean(value);
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
			
			int masterNodePort = sdb.getReplicaGroup(rgName).getMaster().getPort();
			String shell = "ps -ef | grep sequoiadb\\(" + masterNodePort +" | awk '{print $2}'";
			
			try {
				pro = Runtime.getRuntime().exec(new String[]{"sh" , "-c" , shell});
			} catch (IOException e) {
				e.printStackTrace();
			}
			BufferedReader br = new BufferedReader(new InputStreamReader( pro.getInputStream() ) ) ;
			try {
				String killpro = "kill -9 " + br.readLine();
				Runtime.getRuntime().exec(new String[]{"sh","-c",killpro});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		catch(BaseException e)
		{
			if (failonerror)
			{
				throw new BuildException(e.toString());
			}
			else
			{
				log("failed to kill masterNode process" + e);
			}
		}
	}
  
}
