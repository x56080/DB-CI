/**
 * 
 */
package com.sequoiadb.ant.sdbtask;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Parameter;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;

/**
 * @author qiushanggao
 *
 */
public class SdbTest  extends Task{
	private String maxWaitTime = "30m";
	
	private String hostName = "localhost";
	
	private String scriptFileName;
	
	private List<Parameter> params = new ArrayList<Parameter>();
	
	public void setHost(String value)
	{
		hostName = value;
	}
	
	public void setTestscript(String value)
	{
		scriptFileName = value;
	}
	
	public void setTimeout(String value)
	{
		maxWaitTime = value;
	}
	
	public Parameter createParam()
	{
		Parameter param = new Parameter();
		params.add(param);
		return param;
	}
	
	
	public void execute() {
		try{
			STAFHandle handle = new STAFHandle("ant-sdbtasks");
			try{
				//Staf PROCESS START  SHELL COMMAND  ant -l ${test.machine.deploy.path}/install-basic-in-host.log -f ${test.machine.deploy.path}/install-basic-in-host.xml -Dtest.basedir=${test.machine.deploy.path} -Ddeploy.filename=${deploy.tar.file.name} WORKDIR ${test.machine.deploy.path} WAIT 30m
				
				String request = "START SHELL COMMAND ant -f " + scriptFileName;
				
				for(Parameter param: params)
				{
					request += " -D" + param.getName();
					request += "=" + param.getValue();
				}
				
				request += " WAIT " + maxWaitTime;
				
				System.out.println("exec: staf " + hostName + " PROCESS " + request);
				STAFResult result = handle.submit2(hostName, "PROCESS", request);
				
				System.out.println(result.toString());
				if (result.rc != STAFResult.Ok)
				{
					throw new BuildException(result.toString());
				}
				
			}
			finally{
				handle.unRegister();
			}
		}
		catch (STAFException e)
		{
			String errorMsg = "STAFException, RC=" + e.rc + "\nmsg=" + e.getMessage();
			System.out.println(errorMsg);
			e.printStackTrace();
			
			throw new BuildException(errorMsg);
		}
	}
}
