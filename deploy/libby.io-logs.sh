#!/bin/bash

# note: you need to add your ssh keys to ~/.ssh/authorized_keys to be able to use this
# usage: ./libby.io-logs.sh
# then find logs in $project-dir/log/remote-logs/
# most recent log is libby.log.
# don't forget to chmod +x libbyio-logs.sh

host=ubuntu
ip=45.56.99.26

ssh $host@$ip "tar -czf folder-of-logs -C /home/ubuntu/libby/log ." &&

scp $host@$ip:/home/ubuntu/folder-of-logs ./ &&

mkdir -p ../log/remote-logs &&

tar xf folder-of-logs -C ../log/remote-logs/ &&

rm folder-of-logs
