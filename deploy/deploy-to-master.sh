#!/usr/bin/expect

#; usage: `chmod 755 deploy-to-master.sh`
# `./deploy-to-master.sh`
# you do have to watch terminal output to make sure things happen correctly,
# but you don't have to type anything.
# i.e. if some step errors, the rest of the script will keep running FYI.

set timeout 600 ; # number of seconds to wait for "expect [prompt]". `lein uberjar` takes forever.

spawn ssh ubuntu@45.56.99.26

expect "password: "

; #if we ever open-source this, this password should be put in a parameter. or we should use ssh keys.
send "dogshoeexitapple\r"


expect "$ "

send "sudo kill \$(pidof java)\r"

expect "password for ubuntu: "

# ; this password too
send "dogshoeexitapple\r"

expect "$ "

send "cd /home/ubuntu/libby\r"

expect "$ "

send "git checkout master\r"

expect "$ "

send "git pull\r"

expect "$ "

send "lein uberjar\r"

expect "$ "

send "sudo java -jar target/uberjar/libby.jar &\r" ; # `&` means "asynchronous",
# so even though the script ends right after this command, the jar is executing.

expect "$ "

send "exit\r"
