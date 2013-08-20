package com.sequoiadb.ant.sdbtask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;

/**
 * @author chenzichuan
 */
public class SdbCopyAndTar extends Task{
	
	private String localHostName ; 
	private String hostName ; 
	private String diaglogPath =null ; 
	private String savePath ; 
	private String tarPath = null ;
	private String buildNum = "" ;

	
	public void setLocalHostName( String value )
	{
		this.localHostName = changeHostName( value ) ;
	}
	private String changeHostName( String value )
	{
		final String hostName1 = "suse-test1";
		final String hostName2 = "suse-test2";
		final String hostName3 = "suse-test3";
		final String hostName4 = "suse-test4";
		String varHostName = value ;
		if( value.equals( hostName1 ) ) varHostName = "suse-test1.control" ; 
		if( value.equals( hostName2 ) ) varHostName = "suse-test2.control" ; 
		if( value.equals( hostName3 ) ) varHostName = "suse-test3.control" ; 
		if( value.equals( hostName4 ) ) varHostName = "suse-test4.control" ;
		return varHostName ;
	}
	public void setTarPath( String value )
	{
		this.tarPath = value ;
	}
	public void setBuildNum( String value )
	{
		this.buildNum = "." + value ;
	}
	public void setHostName( String value )
	{
		this.hostName = changeHostName( value ) ;
	}
	public void setdiaglogPath( String value )
	{
		this.diaglogPath = value ; 
	}
	public void setSavePath( String value )
	{
		this.savePath = value ; 
	}
	
	private String STAFResultToString(STAFResult result) {

		String msg = "RC=" + result.rc + "\nmsg=" + result.result;
		return msg;
	}
	private String write_file()
	{
		String file_string = null ;
		
		file_string = "#!/bin/bash \n"
				+ "checkDiaglog() \n" 
				+ "{ \n"
				+ "if test -d $1/diaglog \n"
				+ "then \n"
				+ "   echo $1/diaglog is exit ; \n"
				+ "   DIAL_NAME=$1 ; \n"
				+ "   DIAL_NAME=${DIAL_NAME##*/} ; \n"
				+ "   mkdir -p $2\"/\"$DIAL_NAME ; \n"
				+ "   cp -r \"$1/diaglog/\"  \"$2/$DIAL_NAME\" ; \n"
				+ "else \n"
				+ "   for fileName in $(ls $1) \n"
				+ "   do \n"  
				+ "      if test -d $1\"/\"$fileName \n"
				+ "      then \n"
				+ "         checkDiaglog $1\"/\"$fileName $2 ; \n"
				+ "      fi \n"
				+ "   done \n"
				+ "fi \n"
				+ "} \n"
				+ "\n"
				+ "if test -d $1 \n"
				+ "then \n"
				+ "   BASE_DIR=$(readlink -f $0) ; \n"
				+ "   BASE_DIR=$(dirname $BASE_DIR); \n"
				+ "   rm -rf $BASE_DIR\"/\"" + this.hostName + "-diaglog ; \n"
				+ "   mkdir -p $BASE_DIR\"/" + this.hostName + "-diaglog\" ; \n"
				+ "   DIAL_DIR=$BASE_DIR\"/" + this.hostName + "-diaglog\" ; \n"
				+ "   checkDiaglog $1 $DIAL_DIR ; \n"
				+ "fi \n"
				+ "\n"
				+ "tar -zcvf $BASE_DIR/" + this.hostName + "-diaglog.tar.gz  $BASE_DIR\"/" + this.hostName+ "-diaglog\" ; \n" 
				+ ""
				+ ""
				+ ""
				+ ""
				
				;
		return file_string ;
	}
	public void execute ()
	{
		STAFHandle handle = null;
		try{
			
			String doWork = null ;
			String request = null ;
			STAFResult result = null ;
			String copyPath = null ;
			
			
			if( this.tarPath == null && this.diaglogPath != null )
			{
				String now_dir = System.getProperty("user.dir") ; 
				String fileName = now_dir + "/shWork.sh" ;
				File file = new File( fileName ) ;
				if( ! file.exists() ){
					if( ! file.createNewFile() )
						throw new Exception( "create shWork.sh file fail" ) ; 
				}
				
	
				BufferedWriter output = new  BufferedWriter( new FileWriter( file ) ) ; 
				output.write( this.write_file() ) ;
				output.close() ;
				
				
				
				doWork = " chmod a+x  " + this.diaglogPath + "/shWork.sh ; "
						+ "  " 
						+ this.diaglogPath + "/shWork.sh   " + this.diaglogPath + " ; " ; 
						//+ " rm  " + this.diaglogPath + "/shWork.sh ; " ; 
				
				
				
				
				
				handle = new STAFHandle("ant-sdbtasks");
				
				request = "  COPY FILE   " + fileName + "    TODIRECTORY  " + this.diaglogPath 
						+ "   TOMACHINE     " + this.hostName ; 
				log( "exec : staf   " + localHostName + "   " + request ) ; 
				result = handle.submit2( localHostName ,  "FS", request);
				log(STAFResultToString(result));
				if (result.rc != STAFResult.Ok) {
					throw new BuildException(STAFResultToString(result));
				}
				
				
				log(" shWork.sh file : \n" + this.write_file() ) ;
				
				copyPath = this.diaglogPath ;
				
			}
			else{
				doWork = " tar -zcvf  " + this.hostName + "-diaglog.tar.gz" + this.buildNum 
						+ "  " + this.tarPath + "/ ; " ;
				copyPath = this.tarPath ;
			}
			
			request = "START SHELL COMMAND " + doWork + " WAIT 30m " ; 
			
			log("exec: staf " + this.hostName + " PROCESS " + request);
			result = handle.submit2( this.hostName ,  "PROCESS", request);
			log(STAFResultToString(result));
			if (result.rc != STAFResult.Ok) {
				throw new BuildException(STAFResultToString(result));
			}
			
			request = "COPY FILE  " 
					+ copyPath + "/" + this.hostName + "-diaglog.tar.gz" + this.buildNum 
					+ " TODIRECTORY " + this.savePath + " TOMACHINE "
					+ this.localHostName ;

			log("exec: staf " + this.hostName + " FS " + request);
			result = handle.submit2(this.hostName, "FS", request);

			log(STAFResultToString(result));
			if (result.rc != STAFResult.Ok) {
				throw new BuildException(STAFResultToString(result));
			}
			/*
			request = "DELETE ENTRY " + copyPath + "/" + this.hostName + "-diaglog.tar.gz" 
					+ " RECURSE CONFIRM";
			log("exec: staf " + this.hostName + " FS " + request);
			result = handle.submit2(this.hostName, "FS", request);

			log(STAFResultToString(result));
			if (result.rc != STAFResult.Ok) {
				throw new BuildException(STAFResultToString(result));
			}
			
			if( ! file.delete() ){
				throw new Exception( "delete shWork.sh file fail" ) ; 
			}
			*/
		}catch (STAFException e) {
			String errorMsg = "STAFException, RC=" + e.rc + "\nmsg="
					+ e.getLocalizedMessage();
			log(errorMsg);
			e.printStackTrace();
	
			throw new BuildException(errorMsg);
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				if (handle != null)
				{
					handle.unRegister();
				}
			} catch (STAFException e) {
				String errorMsg = "STAFException, RC=" + e.rc + "\nmsg="
						+ e.getLocalizedMessage();
				log(errorMsg);
			}

			handle = null;
			System.gc();
		}
	}

}
