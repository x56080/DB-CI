package com.sequoiadb.ant.tools;


import java.util.ArrayList;
import java.util.List;


public class setPropertyInfo {
	
	private List<sdbProperty> listPro = new ArrayList<sdbProperty>();
	
	public void createSdbProperty()
	{
		listPro.add( new sdbProperty() ) ; 
	}
	public List<sdbProperty> getListPro()
	{
		return this.listPro ; 
	}
}
