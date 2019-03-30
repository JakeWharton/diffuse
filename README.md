Dex Member List
===============

A simple utility which lists all method or field references in an `.apk`, `.aar`, `.dex`, `.jar`,
and/or `.class` files (and any combination of those).

Build by calling `./gradlew clean assemble`. Run `./build/dex-member-list` or by passing in one or
more arguments or by piping data through stdin.

For example:
```
$ ./build/dex-member-list src/test/resources/types.dex
Types <init>()
Types returnsBoolean() → boolean
Types returnsRunnable() → Runnable
Types returnsString() → String
Types test(String)
Types test(String[])
Types test(boolean)
Types test(byte)
Types test(char)
Types test(double)
Types test(float)
Types test(int)
Types test(long)
Types test(short)
Types valueBoolean: boolean
Types valueByte: byte
Types valueChar: char
Types valueDouble: double
Types valueFloat: float
Types valueInt: int
Types valueLong: long
Types valueShort: short
Types valueString: String
Types valueStringArray: String[]
java.lang.Object <init>()
```

Pass `--methods` or `--fields` to list only methods or fields, respectively:

```
$ ./build/dex-member-list --methods src/test/resources/types.dex
Types <init>()
Types returnsBoolean() → boolean
Types returnsRunnable() → Runnable
Types returnsString() → String
Types test(String)
Types test(String[])
Types test(boolean)
Types test(byte)
Types test(char)
Types test(double)
Types test(float)
Types test(int)
Types test(long)
Types test(short)
java.lang.Object <init>()

$ ./build/dex-member-list --fields src/test/resources/types.dex
Types valueBoolean: boolean
Types valueByte: byte
Types valueChar: char
Types valueDouble: double
Types valueFloat: float
Types valueInt: int
Types valueLong: long
Types valueShort: short
Types valueString: String
Types valueStringArray: String[]
```

You can also use the `dex-method-list` or `dex-field-list` binaries for the same effect.

By default the D8 compiler is used to dex any class files. Pass the `--legacy-dx` flag to use the
old DX compiler.

Use the `--hide-synthetic-numbers` flag to remove number suffix from synthetic accessor
methods. This is useful to prevent noise when `diff`ing output.

All binaries support the `--help` flag for more information about invocation.

You can also use this tool as a library. Add a dependency on
`com.jakewharton.dex:dex-member-list:3.3.0` and use `DexParser`.


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
