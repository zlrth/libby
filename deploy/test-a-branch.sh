#!/usr/bin/expect

; # this script doesn't work yet

set timeout 600 ; # number of seconds to wait for "expect [prompt]". `lein uberjar` takes forever.
set branchname [lindex $argv 0]

spawn ssh ubuntu@45.56.99.26

expect "password: "

send "dogshoeexitapple\r"

expect "$ "

#; send "sudo kill \$(pidof java)\r"

expect "password for ubuntu: "

send "dogshoeexitapple\r"

expect "$ "

send "cd /home/ubuntu/libby\r"

expect "$ "

send "git checkout $branchname\r"

expect "$ "

send "git pull\r"

expect "$ "

send "lein run &\r"

expect "$ "

send "exit\r"
