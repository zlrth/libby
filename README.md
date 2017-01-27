# libby

## configuring your host machine for the sql database

first you install mysql, following some of these directions: https://coderwall.com/p/os6woq/uninstall-all-those-broken-versions-of-mysql-and-re-install-it-with-brew-on-mac-mavericks . ignore most of it. just do like brew update and brew upgrade and brew install and then (important!):

- mysql.server start
- mysql -uroot
- then, inside mysql: create database bookwarrior;
- now exit mysql with control-d.
- now, assuming everything's done, do this: mysql -u root -p bookwarrior < backup_libgen.sql
- hit enter when prompted for a password
- it should take minutes.
- you are ready to learn sql queryies!
- once you've mysql -uroot'd, don't forget to use bookwarrior;
- to check that it works, try select title from updated where md5 = '67BA5CC5A7118784770ED996B348C0FD';
- the result should be: Clojure Applied: From Practice to Practitioner








possibly-useful link:

http://stackoverflow.com/questions/17666249/how-to-import-an-sql-file-using-the-command-line-in-mysql

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start the db, run:

    mysqld &
    
To start a web server for the application, run:

    lein run

## License

Copyright © 2017 FIXME
