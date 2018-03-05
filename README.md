Dex Method/Field List
=====================

A simple utility which lists all method or field references in an `.apk`, `.aar`, `.dex`, `.jar`,
and/or `.class` files (and any combination of those).

Build by calling `./gradlew clean assemble`. Run `./build/dex-method-list` or
`./build/dex-field-list` by passing in one or more arguments or by piping data through stdin.

For example:
```
$ ./build/dex-method-list src/test/resources/types.dex
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

$ ./build/dex-method-list src/test/resources/one.apk
Params <init>()
Params test(String, String, String, String)
java.lang.Object <init>()

$ ./build/dex-method-list build/classes/main/com/jakewharton/dex/DexMethods.class
com.jakewharton.dex.DexMethods <clinit>()
com.jakewharton.dex.DexMethods <init>()
com.jakewharton.dex.DexMethods access$getCLASS_MAGIC$cp() → byte[]
com.jakewharton.dex.DexMethods access$getDEX_MAGIC$cp() → byte[]
com.jakewharton.dex.DexMethods access$getSYNTHETIC_SUFFIX$cp() → Regex
com.jakewharton.dex.DexMethods list(File[]) → List
com.jakewharton.dex.DexMethods list(Iterable) → List
com.jakewharton.dex.DexMethods list(Iterable, boolean) → List
com.jakewharton.dex.DexMethods list(byte[]) → List
com.jakewharton.dex.DexMethods main(String[])
com.jakewharton.dex.DexMethods$Companion <init>(DefaultConstructorMarker)
com.jakewharton.dex.DexMethods$Companion list(File[]) → List
com.jakewharton.dex.DexMethods$Companion list(Iterable) → List
com.jakewharton.dex.DexMethods$Companion list(Iterable, boolean) → List
com.jakewharton.dex.DexMethods$Companion list(byte[]) → List
com.jakewharton.dex.DexMethods$Companion main(String[])
java.lang.Object <init>()
kotlin.jvm.internal.Intrinsics checkParameterIsNotNull(Object, String)
kotlin.text.Regex <init>(String)

$ ./build/dex-method-list ~/.m2/repository/com/google/guava/guava/19.0/guava-19.0.jar | head -10
byte[] clone() → Object
com.google.common.annotations.GwtCompatible emulated() → boolean
com.google.common.annotations.GwtCompatible serializable() → boolean
com.google.common.annotations.GwtIncompatible value() → String
com.google.common.base.Absent <clinit>()
com.google.common.base.Absent <init>()
com.google.common.base.Absent asSet() → Set
com.google.common.base.Absent equals(Object) → boolean
com.google.common.base.Absent get() → Object
com.google.common.base.Absent hashCode() → int

$ ./build/dex-field-list src/test/resources/types.dex
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

Use the `--hide-synthetic-numbers` argument to remove number suffix from synthetic accessor
methods. This is useful to prevent noise when `diff`ing output.

You can also use this tool as a library. Add a dependency on
`com.jakewharton.dex:dex-method-list:3.0.0` and use the `DexMethods.list` or `DexFields.list`
methods.


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
