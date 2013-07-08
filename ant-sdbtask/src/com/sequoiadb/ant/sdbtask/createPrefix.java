/**
*
*/
package com.sequoiadb.ant.sdbtask;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Parameter;

public class createPrefix extends Task{
   
   private List<Parameter> params = new ArrayList<Parameter>();
   
   public Parameter createParam(){
		 
		 Parameter param = new Parameter();
		 
		 params.add(param);
		 
		 return param;
	 }
   
   public void execute(){
      
      	
      String request = "" ;
      	 
      String lineNum = Integer.toString( (int)(Math.random()*1000) );
      	 
      for ( Parameter param : params ){
      	    
      	 request += param.getValue() ; 	
      	    
      }
      request += lineNum ; 
      	 
      this.getProject().setProperty( "CS_PRIX" , request.replaceAll( "[-_]" , "") ) ; 


   	
   }
	
}
