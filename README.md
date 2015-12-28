Dex Method List
===============

A simple utility which lists all method references in a dex file.

Build by calling `./gradlew clean build`. Run `./build/exec/dex-method-list` by passing in an dex
file or piping one through stdin.

For example:
```
$ ./build/exec/dex-method-list test.dex
$ unzip -p app.apk classes.dex | ./build/exec/dex-method-list
```
