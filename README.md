Dex Method List
===============

A simple utility which lists all method references in a dex file.

Build by calling `./gradlew clean build`. Run `./build/exec/dex-method-list` by passing in one or
more dex or apk files as arguments or piping one through stdin.

For example:
```
$ ./build/exec/dex-method-list test.dex
$ ./build/exec/dex-method-list one.dex two.dex three.dex
$ ./build/exec/dex-method-list test.apk
```


License
-------

    Copyright 2015 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
