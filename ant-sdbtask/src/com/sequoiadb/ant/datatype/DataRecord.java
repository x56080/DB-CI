/**
 * 
 */
package com.sequoiadb.ant.datatype;

import org.bson.BSONObject;
import org.bson.util.JSON;

/**
 * @author qiushanggao
 *
 */
public class DataRecord {

	private String text = null;
	
	
	public void addText(String value)
	{
		text = value;
	}
	
	public BSONObject toBSONObj()
	{
		BSONObject obj = null;
		if (text != null)
		{
			obj = (BSONObject) JSON.parse(text);
		}
		return obj;
	}
}
