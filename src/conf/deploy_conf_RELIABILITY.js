/******************************************************
@decription:   configure for install_deploy/deploy_tpcc.js
               only variable defined
@author:       CSQ 2017-03-22   
******************************************************/

var mode = "cluster";

var cmPort          = 11790;
var tmpCoordPort    = 18800;
var coordnumPerhost = 1;
var cataNum         = 3;                           //total catalog number
var datagroupNum    = 3;
var replSize        = 3;
var diskList        = [                             //disks for dbPath
                        INSTALL_DIR
                      ];
                     
if( typeof( diagLevel ) === "undefined" ) 
{
   var diagLevel = 3;
}

var cataConf  = { diaglevel:diagLevel,
                  sharingbreak:30000,
                  diagnum:60,
                  ftfusingtimeout:300,
                  logfilenum:40,
                  diagpath: INSTALL_DIR + '/database/cata/[svcname]/diaglog',
                };
var coordConf = { diaglevel:diagLevel,
                  diagnum:60,
                  ftfusingtimeout:300,
                  logfilenum:40,
                  diagpath: INSTALL_DIR + '/database/coord/[svcname]/diaglog',
                };
var dataConf  = { diaglevel:diagLevel,
                  sharingbreak:30000,
                  diagnum:60,
                  ftfusingtimeout:300,
                  logfilenum:40,
                  diagpath: INSTALL_DIR + '/database/data/[svcname]/diaglog',
                  ftmask:'NONE',
                  indexpath: METADATA_DIR + '/[svcname]/',
                  lobmetapath:  METADATA_DIR + '/[svcname]/',
                };
                