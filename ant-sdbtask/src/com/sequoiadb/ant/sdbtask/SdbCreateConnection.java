/**
 * 
 */
package com.sequoiadb.ant.sdbtask;

import java.util.UUID;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;

/**
 * @author qiushanggao
 * 
 */
public class SdbCreateConnection extends Task {

	private String hostName = null;
	private String port = null;
	private String refPropertyName = null;

	public void setHostname(String value) {
		this.hostName = value;
	}

	public void setPort(String value) {
		this.port = value;
	}

	public void setSdbrefproperty(String value) {
		this.refPropertyName = value;
	}

	public void execute() {
		try {
			Sequoiadb sdb = new Sequoiadb(this.hostName,
					Integer.parseInt(this.port), "", "");

			UUID uuid = UUID.randomUUID();
			String strUUID = uuid.toString();

			this.getProject().addReference(strUUID, sdb);
			this.getProject().setProperty(this.refPropertyName, strUUID);
		} catch (BaseException e) {
			e.printStackTrace();
			throw new BuildException(e.toString());
		}
	}
}
