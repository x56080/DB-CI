/****************************************************************
@decription:   deploy for performance test
               excute cmd: bin/sdb -f 'conf/deploy_conf_TPCC.js,install_deploy/deploy_tpcc.js' 
                           -e 'var hostList=["ci-test-pm1","ci-test-pm2","ci-test-pm3"];'
@input:        hostList: e.g.['host1','host2','host3'], G1D3 and G3D3 need 3 hosts
@author:       Ting YU 2017-02-06   
****************************************************************/
if( typeof(hostList) === "undefined" ) 
{ 
   throw "invalid para: hostList, can not be null"; 
}
if( hostList.constructor !== Array ) 
{ 
   throw "invalid para: hostList, should be array"; 
}

var hostNum = hostList.length;
var tmpCoordHost = hostList[0];
var retryTimes = 3 ;
var version='';

for (var n = 0; n < retryTimes; ++n)
{
   try
   {
      main();
      break ;
   }
   catch(e)
   {
      println( "no:" + n +"err:" + e);
      if ( n == retryTimes - 1)
      {
         throw e;
      }
      sleep(180000);
   }
}

function main()
{

   println("check cluster sdbcm status...");
   checkSdbcm()

   println("begin deploy...");
   if( mode === "standalone" )
   {
      removeStandaloneNode();
      deployStandalone();
      checkSdbcm()
   }
   else
   {
      removeCluster();
      deployCluster();
   }
}

function updateConf( db )
{
   try
   {
	  if(version.indexOf("3.4")==-1){
        db.getRecycleBin().alter({Enable:true,MaxItemNum:30,AutoDrop:true});
      }

   }
   catch(e)
   {
      throw new Error(e) ;
   }
}

 function checkSdbcm()
 {
     var ansibleCmd="cd " + ansibleDir + ";ansible sdb -m raw -a 'sdbcmart'"
     for ( i = 0; i < hostNum; ++i) {
         try
         {
     	    println( "-----check sdbcm in " + hostList[i] );
            hostNameorIp=hostList[i];
            new Remote (hostNameorIp, cmPort );
         }
         catch(e)
         {
            println("exception: " + e + ", try restart sdbcm" );
            var ret = new Ssh('localhost','sdbadmin','Admin@1024',22).exec(ansibleCmd)
            println("ret: " + ret );
         }
     }
 }

 function restartSdbcm(hostNameorIp)
 {
     try
     {
        var ssh = new Ssh(hostNameorIp,"sdbadmin", "Admin@1024",22)
        ssh.exec("sdbstop --all && sdbcmtop && sdbcmart");

        var remote = new Remote (hostNameorIp, cmPort );
        var installDir = getInstallDir(remote);
        version = remote.getCmd().run( installDir + "/bin/sdb -version|awk -F':' 'index($1, \"SequoiaDB\"){print $2}'").trim()
        println("sdb version: " + version)
     }
     catch(e)
     {
        println("exec sdbstop --all && sdbcmtop && sdbcmart:" + e);
        // ignore error
     }
 }


function removeCluster()
{
    println("begin remove Cluste") ;
    removeCataNode();
    removeCoordNode();
    removeDataNode();

    for ( i = 0; i < hostNum; ++i)
    {
	   println( "-----begin to restart sdbcm in " + hostList[i] );
      restartSdbcm(hostList[i]);
    }
}

function deployCluster()
{
   println("------deploy mode: H" + hostNum + "G" + datagroupNum + "D" + replSize );
   try
   {
      var db = createTmpCoord();
      createCata( db );
      createCoord( db );
      createData( db );
      updateConf( db ) ;
   }
   catch(e)
   {
      throw e;
   }
   finally
   {
      clean( db );
   }
   println("------succed to deploy");
}

function removePath(remote, fileOrPath)
{
   try
   {
      remote.getCmd().run("rm -rf " + fileOrPath );
   }
   catch(e)
   {
      println("rm -rf " + fileOrPath + " err:" + e);
      // ignore error
   }
}

function getInstallDir(remote)
{
   try
   {
      //var installdir=remote.getCmd().run("cat /etc/default/sequoiadb|grep INSTALL_DIR").split("\n")[0]
      //return installdir.split("=")[1];
      return INSTALL_DIR;
   }
   catch(e)
   {
      return "";
   }
}

function removeStandaloneNode()
{
   for( var i in hostList )
   {
      var host = hostList[i];
      println( "-----begin to remove node in " + host );

      var remote = new Remote (host, cmPort );
      var service = 11810;
      var dbPath = diskList[0] + "/database/standalone/" + service;
      removePath(remote, dbPath);
      var confPath = getInstallDir(remote) + "/conf/local/" + service
      removePath(remote, confPath) ;
      println( "-----end to remove node in " + host );
      var config = updateDeployConfig( nodeConf, service );
      if ( typeof(config.indexpath) != "undefined" )
      {
         removePath(remote, config.indexpath) ;
      }

      if ( typeof(config.lobmetapath) != "undefined" )
      {
        removePath(remote, config.lobmetapath) ;
      }

      println( "-----begin to restart sdbcm in " + host );
      restartSdbcm(host) ;
   }
}

function deployStandalone()
{
   println("------deploy mode: STANDALONE");

   for( var i in hostList )
   {
      var host = hostList[i];
      println( "-----begin to create node in " + host );

      var oma = new Oma( host, cmPort );

      var service = 11810;
      var dbPath = diskList[0] + "/database/standalone/" + service;
      var config = updateDeployConfig( nodeConf, service );
      oma.createData( service, dbPath, config );

      oma.startNode( service );
   }

   println("------succed to deploy");
}

function createTmpCoord()
{
   println( "-----begin to create and link temp coord" );

   var oma = new Oma( tmpCoordHost, cmPort );

   var dbBasePath = diskList[0];
   var dbPath= dbBasePath + "/database/coord/" + tmpCoordPort
   oma.createCoord( tmpCoordPort, dbPath );
   println( "createTmpCoordNode(" + tmpCoordHost + "," + tmpCoordPort + "," + dbPath +")" );
   oma.startNode( tmpCoordPort );

   var db = new Sdb( tmpCoordHost, tmpCoordPort );

   return db;
}

function removeCataNode()
{
   var cataBasePort = 11800;
   var i = 0;
   while( i < cataNum )
   {
      var host = hostList[ i % hostNum ];
      var service = cataBasePort + parseInt( i / hostNum ) * 20;
      var dbPath = diskList[0] + "/database/cata/" + service;
      println("remove " + host + " " + dbPath );
      var remote = new Remote(host, cmPort);
      removePath(remote, dbPath) ;

      var confPath = getInstallDir(remote) + "/conf/local/" + service
      println("remove " + host + " " + confPath );
      removePath(remote, confPath) ;
      var config = updateDeployConfig( cataConf, service );
      if ( typeof(config.indexpath) != "undefined" )
      {
         removePath(remote, config.indexpath) ;
      }

      if ( typeof(config.lobmetapath) != "undefined" )
      {
        removePath(remote, config.lobmetapath) ;
      }

      i++;
   }

}

function createCata( db )
{
   println("-----begin to create cata group");
   var cataBasePort = 11800;

   //create first catalog node
   var host = hostList[0];
   var service = cataBasePort;
   var dbPath = diskList[0] + "/database/cata/" + service;
   var config = updateDeployConfig( cataConf, service );
   println( "createCataRG(" + host + "," + service + "," + dbPath +")" );
   var rg = db.createCataRG( host, service, dbPath, config );

   //wait for cata group to select primary node
   for(var i = 0; i < 600; i++ )
   {
      try
      {
         sleep(100);
         var rg = db.getRG("SYSCatalogGroup");
         break;
      }
      catch(e)
      {
         if( e !== -71 ) throw e;
      }
   }

   //create other catalog nodes
   var i = 0;
   while( i < cataNum )
   {
      if( i === 0 )
      {
         i++;
         continue;      //first cata node has been already created
      }

      var host = hostList[ i % hostNum ];
      var service = cataBasePort + parseInt( i / hostNum ) * 20;
      var dbPath = diskList[0] + "/database/cata/" + service;
      var config = updateDeployConfig( cataConf, service );

      println( "createCataNode(" + host + "," + service + "," + dbPath +")" );
      rg.createNode( host, service, dbPath, config );

      i++;
   }

   //start other nodes
   var i = 0;
   while( i < cataNum )
   {
      if( i === 0 )
      {
         i++;
         continue;      //first cata node has been already started
      }

      var host = hostList[ i % hostNum ];
      var service = cataBasePort + parseInt( i / hostNum ) * 20;
      rg.getNode( host, service ).start();

      i++;
   }
   checkeCataPrimary( db, "SYSCatalogGroup" );
}

function removeCoordNode()
{
    var coordBasePort = 11810;
    var dbBasePath = diskList[0];
    for( var i in hostList )
    {
       for( var j = 0; j < coordnumPerhost; j++ )
       {
          var host = hostList[i];
          var service = coordBasePort + j * 20;
          var dbPath = dbBasePath + "/database/coord/" + service;
          println("remove " + host + " " + dbPath );
          var remote = new Remote (host, cmPort );
          removePath(remote, dbPath);
          var confPath = getInstallDir(remote) + "/conf/local/" + service
          println("remove " + host + " " + confPath );
          removePath(remote, confPath) ;
          var config = updateDeployConfig( coordConf, service );
          if ( typeof(config.indexpath) == "undefined" )
          {
             removePath(remote, config.indexpath) ;
          }

          if ( typeof(config.lobmetapath) == "undefined" )
          {
             removePath(remote, config.lobmetapath) ;
          }

       }
    }

}

function createCoord( db )
{
   println("-----begin to create coord group");
   var coordBasePort = 11810;
   var dbBasePath = diskList[0];

   var rg = db.createCoordRG();

   for( var i in hostList )
   {
      for( var j = 0; j < coordnumPerhost; j++ )
      {
         var host = hostList[i];
         var service = coordBasePort + j * 20;
         var dbPath = dbBasePath + "/database/coord/" + service;
         var config = updateDeployConfig( coordConf, service );
         println( "createCoordNode(" + host + "," + service + "," + dbPath +")" );
         rg.createNode( host, service, dbPath, config );
      }

   }

   rg.start();
}

function removeDataNode()
{
   var dataBasePort = 20000;

   for( var n = 0; n < datagroupNum; n++ )
   {
      var dataRgBasePort = dataBasePort + ( n + 1 ) * 100;
      if( n === 0 )
      {
         var randomHostList = hostList;
      }
      else
      {
         var randomHostList = randomArray( randomHostList );
      }
      var i = 0;
      while( i < replSize )
      {
         var host = randomHostList[ i % hostNum ];
         var service = dataRgBasePort + parseInt( i / hostNum ) * 10;
         if( diskList.length === 1 )
         {
           var dbPath = diskList[0] + "/database/data/" + service;
         }
         else
         {
             var dbPath = diskList[ n + 1 ] + "/database/data/" + service;
         }
         var remote = new Remote (host, cmPort );
         println("remove " + host + " " + dbPath );
         removePath(remote, dbPath);
         var confPath = getInstallDir(remote) + "/conf/local/" + service
         println("remove " + host + " " + confPath );
         removePath(remote, confPath) ;
         var config = updateDeployConfig( dataConf, service );
         if ( typeof(config.indexpath) != "undefined" )
         {
            removePath(remote, config.indexpath) ;
         }

         if ( typeof(config.lobmetapath) != "undefined" )
         {
           removePath(remote, config.lobmetapath) ;
         }

         i++;
       }
   }
}

function createData( db )
{
   var dataBasePort = 20000;

   for( var n = 0; n < datagroupNum; n++ )
   {
      //create group
      var datargName = "group" + ( n + 1 );
      println( "-----begin to create data group: " + datargName );
      var rg = db.createRG( datargName );

      //create node
      var dataRgBasePort = dataBasePort + ( n + 1 ) * 100;
      if( n === 0 )
      {
         var randomHostList = hostList;
      }
      else
      {
         var randomHostList = randomArray( randomHostList );
      }

      var i = 0;
      while( i < replSize )
      {
         var host = randomHostList[ i % hostNum ];
         var service = dataRgBasePort + parseInt( i / hostNum ) * 10;
         if( diskList.length === 1 )
         {
            var dbPath = diskList[0] + "/database/data/" + service;
         }
         else
         {
            var dbPath = diskList[ n + 1 ] + "/database/data/" + service;
         }
         var config = updateDeployConfig( dataConf, service );
         println( "createDataNode(" + host + "," + service + "," + dbPath +")" );
         rg.createNode( host, service, dbPath, config );

         i++;
      }

      //start node
      rg.start();
      checkeDataPrimary( db, datargName );
   }
}

function updateDeployConfig( conf, service )
{
   var config = JSON.stringify(conf).replace( "[svcname]", service );
   var config = config.replace( /\[svcname\]/g, service );
   return JSON.parse(config);
}

function checkeCataPrimary( db, rgname )
{
   var hasPrimary = false;
   for(var i = 0; i < 5*600; i++ )  //wait for cata group to select primary node
   {
      try
      {
         sleep(100);
         var cataRG = db.getRG("SYSCatalogGroup");
         hasPrimary = true;
         break;
      }
      catch(e)
      {
         if( e !== -71 )
         {
            println("excute: db.getRG('SYSCatalogGroup')");
            throw e;
         }
      }
   }
   if( hasPrimary === false )
   {
      throw "fail to select primary node after 5 minute";
   }
}

function checkeDataPrimary( db, rgname )
{
   var hasPrimary = false;
   for(var i = 0; i < 5*600; i++ )  //wait for data group to select primary node
   {
      try
      {
         sleep(100);
         db.getRG(rgname).getMaster();
         hasPrimary = true;
         break;
      }
      catch(e)
      {
         if( e !== -71 )
         {
            println("excute: db.getRG(" + rgname + ").getMaster()");
            throw e;
         }
      }
   }
   if( hasPrimary === false )
   {
      throw "fail to select primary node after 5 minute";
   }
}

function randomArray( arr ) // [1, 2, 3]--> [2, 3, 1]
{
   var firEle = arr.shift();
   arr.push( firEle );
   return arr;
}

function clean( db )
{
	println( "-----begin to remove temp coord" );
	var oma = new Oma( tmpCoordHost, cmPort );
	oma.removeCoord( tmpCoordPort );
	println("remove " + tmpCoordHost + " " + tmpCoordPort );
}


