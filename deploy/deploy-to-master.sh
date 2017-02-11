#!/usr/bin/expect

set timeout 600 ; # number of seconds to wait for "expect [prompt]". `lein uberjar` takes forever.

spawn ssh ubuntu@45.56.99.26

expect "password: "

send "dogshoeexitapple\r"

expect "$ "

send "sudo kill \$(pidof java)\r"

expect "password for ubuntu: "

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
