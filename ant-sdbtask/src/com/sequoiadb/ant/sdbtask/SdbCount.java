package com.sequoiadb.ant.sdbtask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sequoiadb.base.CollectionSpace;
import com.sequoiadb.base.DBCollection;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;

public class SdbCount extends Task {
	private String uuid = null;
	private String CSName = null;
	private String CLName = null;
	private String query = null;
	private String CountProp = null;

	public void setSdbhandle(String value) {
		uuid = value;
	}

	public void setCsname(String value) {
		CSName = value;
	}

	public void setClname(String value) {
		CLName = value;
	}

	public void setQuery(String value) {
		query = value;
	}
	
	public void setCountproperty(String value){
		CountProp = value;
	}

	public void execute() {
		Object obj = this.getProject().getReference(uuid);
		if (!(obj instanceof Sequoiadb)) {
			throw new BuildException("The SdbUUID" + uuid
					+ " cannot get Sequoiadb Object.");
		}

		try {
			Sequoiadb sdb = (Sequoiadb) obj;
			CollectionSpace cs = sdb.getCollectionSpace(CSName);
			DBCollection cl = cs.getCollection(CLName);

			long size = cl.getCount(query);
			
			this.getProject().setProperty(CountProp, Long.toString(size));
			

		} catch (BaseException e) {
			throw new BuildException(e);
		}
	}
}
