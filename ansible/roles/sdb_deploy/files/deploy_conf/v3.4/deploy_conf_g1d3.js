/******************************************************
@decription:   configure for install_deploy/deploy_tpcc.js
               only variable defined
@input:        diagLevel: 0 1 2 3 4 5, default 3
@author:       Ting YU 2017-02-06   
******************************************************/

var mode = "cluster";

var cmPort          = 11790;
var tmpCoordPort    = 18800;
var coordnumPerhost = 1;
var cataNum         = 3;                           //total catalog number
var datagroupNum    = 1;
var replSize        = 3;
var diskList        = [ INSTALL_DIR ];        //disks for dbPath

if( typeof( diagLevel ) === "undefined" ) 
{
   var diagLevel = 3;
}

var osArch = new Cmd().run('arch');
if ( osArch === "x86_64\n" ) 
{ 
   var fapValue = "fapmongo3";
}
else
{
   var fapValue = "";
}

var cataConf  = { diaglevel:diagLevel,
                  sharingbreak:30000,
                  diagnum:60,
                  ftfusingtimeout:300,
                  logfilenum:40,
                  mongroupmask:'all:detail'
                };
var coordConf = { diaglevel:diagLevel,
                  diagnum:60,
                  logfilenum:40,
                  ftfusingtimeout:300,
                  fap:fapValue,
                  mongroupmask:'all:detail'
                };
var dataConf  = { diaglevel:diagLevel,
                  sharingbreak:30000,
                  diagnum:60,
                  ftfusingtimeout:300,
                  logfilenum:40,
                  indexpath: METADATA_DIR + '/[svcname]/',
                  lobmetapath: METADATA_DIR + '/[svcname]/',
                  mongroupmask:'all:detail'
                };
