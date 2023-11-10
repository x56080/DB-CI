#!/usr/bin/expect

#exp_internal 1
set src [lrange $argv 0 0] 
set dst [lrange $argv 1 1]
set timeout -1

spawn su root
expect {
        "Password:" {
                send "Sdb@123123\r"
                exp_continue
        }
        "密码：" {
                        send "Sdb@123123\r"
                        exp_continue
        }
        "*#*" {
                send "mkdir -p $dst\r"
                sleep 1
                send "bash archiveandcheck.sh $src $dst\n"
        }
}

expect "*#*"
send "exit\r"
expect eof
exit
