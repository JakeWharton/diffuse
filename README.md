Dex Method List
===============

A simple utility which lists all method references in `.apk`, `.dex`, `.jar`, and `.class` files.

Build by calling `./gradlew clean assemble`. Run `./build/dex-method-list` by passing in one or
more arguments or piping data through stdin.

For example:
```
$ ./build/dex-method-list src/test/resources/types.dex
Types <init>()
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

$ ./build/dex-method-list src/test/resources/one.apk
Params <init>()
Params test(String, String, String, String)
java.lang.Object <init>()

$ ./build/dex-method-list build/classes/main/com/jakewharton/dex/DexMethods.class
com.jakewharton.dex.DexMethods <clinit>()
com.jakewharton.dex.DexMethods <init>()
com.jakewharton.dex.DexMethods access$getCLASS_MAGIC$cp()
com.jakewharton.dex.DexMethods access$getDEX_MAGIC$cp()
com.jakewharton.dex.DexMethods list(File[])
com.jakewharton.dex.DexMethods list(Iterable)
com.jakewharton.dex.DexMethods list(byte[])
com.jakewharton.dex.DexMethods main(String[])
com.jakewharton.dex.DexMethods$Companion <init>(DefaultConstructorMarker)
com.jakewharton.dex.DexMethods$Companion list(File[])
com.jakewharton.dex.DexMethods$Companion list(Iterable)
com.jakewharton.dex.DexMethods$Companion list(byte[])
com.jakewharton.dex.DexMethods$Companion main(String[])
java.lang.Object <init>()
kotlin.jvm.internal.Intrinsics checkParameterIsNotNull(Object, String)

$ ./build/dex-method-list ~/.m2/repository/com/google/guava/guava/19.0/guava-19.0.jar | head -10
byte[] clone()
com.google.common.annotations.GwtCompatible emulated()
com.google.common.annotations.GwtCompatible serializable()
com.google.common.annotations.GwtIncompatible value()
com.google.common.base.Absent <clinit>()
com.google.common.base.Absent <init>()
com.google.common.base.Absent asSet()
com.google.common.base.Absent equals(Object)
com.google.common.base.Absent get()
com.google.common.base.Absent hashCode()
```

Use the `--hide-synthetic-numbers` argument to remove number suffix from synthetic accessor
methods. This is useful to prevent noise when `diff`ing output.

You can also use this tool as a library. Add a dependency on
`com.jakewharton.dex:dex-method-list:1.2.0` and use the `DexMethods.list` methods.


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
