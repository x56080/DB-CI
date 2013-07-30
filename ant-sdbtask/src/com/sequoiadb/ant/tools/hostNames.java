package com.sequoiadb.ant.tools;


import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.types.Parameter;


public class hostNames {
	
      private List<Parameter> listParam = new ArrayList<Parameter>();

	
	public void createParam()
	{
		listParam.add( new Parameter() ) ; 
	}
	public List<Parameter> getListParameter()
	{
		return this.listParam ; 
	}

}
