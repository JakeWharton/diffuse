Diffuse
=======

Diffuse is a tool for diffing APKs, AABs, AARs, and JARs in a way that aims to provide both a
high-level view of what changes along with important detailed output.

It is meant to be used on small changes, such as those that occur in a single PR or git SHA. Here
is an example of updating the [Dagger](https://github.com/google/dagger/) library in the
[SDK Search](https://github.com/JakeWharton/SdkSearch/) app:

```
$ diffuse diff sdk-search-release-1.apk sdk-search-release-2.apk

OLD: sdk-search-release-1.apk (signature: V2)
NEW: sdk-search-release-2.apk (signature: V2)


          │          compressed           │          uncompressed
          ├───────────┬───────────┬───────┼───────────┬───────────┬────────
 APK      │ old       │ new       │ diff  │ old       │ new       │ diff
──────────┼───────────┼───────────┼───────┼───────────┼───────────┼────────
      dex │ 664.8 KiB │ 664.8 KiB │ -25 B │   1.5 MiB │   1.5 MiB │ -112 B
     arsc │ 201.7 KiB │ 201.7 KiB │   0 B │ 201.6 KiB │ 201.6 KiB │    0 B
 manifest │   1.4 KiB │   1.4 KiB │   0 B │   4.2 KiB │   4.2 KiB │    0 B
      res │ 418.2 KiB │ 418.2 KiB │ -14 B │ 488.3 KiB │ 488.3 KiB │    0 B
    asset │       0 B │       0 B │   0 B │       0 B │       0 B │    0 B
    other │  37.1 KiB │  37.1 KiB │   0 B │  36.3 KiB │  36.3 KiB │    0 B
──────────┼───────────┼───────────┼───────┼───────────┼───────────┼────────
    total │   1.3 MiB │   1.3 MiB │ -39 B │   2.2 MiB │   2.2 MiB │ -112 B


 DEX     │ old   │ new   │ diff
─────────┼───────┼───────┼────────────
   count │     1 │     1 │  0
 strings │ 14220 │ 14218 │ -2 (+6 -8)
   types │  2258 │  2257 │ -1 (+0 -1)
 classes │  1580 │  1579 │ -1 (+0 -1)
 methods │ 11640 │ 11641 │ +1 (+6 -5)
  fields │  4369 │  4369 │  0 (+0 -0)


 ARSC    │ old  │ new  │ diff
─────────┼──────┼──────┼──────
 configs │   51 │   51 │  0
 entries │ 1950 │ 1950 │  0


=================
====   APK   ====
=================

    compressed     │   uncompressed   │
───────────┬───────┼─────────┬────────┤
 size      │ diff  │ size    │ diff   │ path
───────────┼───────┼─────────┼────────┼───────────────────────────────────────────────────────────
 664.8 KiB │ -25 B │ 1.5 MiB │ -112 B │ ∆ classes.dex
     458 B │ -14 B │   272 B │    0 B │ ∆ res/drawable-hdpi-v4/abc_ab_share_pack_mtrl_alpha.9.png
───────────┼───────┼─────────┼────────┼───────────────────────────────────────────────────────────
 665.2 KiB │ -39 B │ 1.5 MiB │ -112 B │ (total)



=================
====   DEX   ====
=================

STRINGS:

   old   │ new   │ diff
  ───────┼───────┼────────────
   14220 │ 14218 │ -2 (+6 -8)

  + %s does not implement %s
  + %s.androidInjector() returned null
  + androidInjector
  + b1b69b7d0f149276095d82b6e0b884f82ac4d3f4
  + getAndroidInjector
  + ~~R8{"compilation-mode":"release","min-api":24,"pg-map-id":"5362c3b","version":"1.5.59"}

  - %s does not implement %s or %s
  - %s.activityInjector() returned null
  - 0e95da1111e6daf6172ec76c544c88764db28334
  - HasActivityInjector.java
  - Ldagger/android/HasActivityInjector;
  - activityInjector
  - getActivityInjector
  - ~~R8{"compilation-mode":"release","min-api":24,"pg-map-id":"3041c7f","version":"1.5.59"}


TYPES:

   old  │ new  │ diff
  ──────┼──────┼────────────
   2258 │ 2257 │ -1 (+0 -1)

  - Ldagger/android/HasActivityInjector;


METHODS:

   old   │ new   │ diff
  ───────┼───────┼────────────
   11640 │ 11641 │ +1 (+6 -5)

  + com.jakewharton.sdksearch.AppComponent getAndroidInjector() → DispatchingAndroidInjector
  + com.jakewharton.sdksearch.DaggerReleaseAppComponent getAndroidInjector() → DispatchingAndroidInjector
  + com.jakewharton.sdksearch.SdkSearchApplication androidInjector() → AndroidInjector
  + com.jakewharton.sdksearch.SdkSearchApplication androidInjector() → DispatchingAndroidInjector
  + dagger.android.AndroidInjection inject(Object, HasAndroidInjector)
  + dagger.android.HasAndroidInjector androidInjector() → AndroidInjector

  - com.jakewharton.sdksearch.AppComponent getActivityInjector() → DispatchingAndroidInjector
  - com.jakewharton.sdksearch.DaggerReleaseAppComponent getActivityInjector() → DispatchingAndroidInjector
  - com.jakewharton.sdksearch.SdkSearchApplication activityInjector() → AndroidInjector
  - com.jakewharton.sdksearch.SdkSearchApplication activityInjector() → DispatchingAndroidInjector
  - dagger.android.HasActivityInjector activityInjector() → AndroidInjector
```


Usage
-----

`diffuse` has multiple subcommands. The primary one is `diff` which takes two binaries and displays
a summary and detailed listing of changes between them.

```
$ diffuse diff old.apk new.apk

$ diffuse diff --aab old.aab new.aab

$ diffuse diff --aar old.aar new.aar

$ diffuse diff --jar old.jar new.jar
```

For a single binary, the `info` subcommand will show a summary table of the binary contents.

```
$ diffuse info my.apk

$ diffuse info --aab my.aab

$ diffuse info --aar my.aar

$ diffuse info --jar my.jar
```

Finally, the `members` subcommand lists the methods, fields, or both of a binary. This mimics
the behavior of `dex-member-list`, the tool from which Diffuse is derived.

```
$ diffuse members my.apk

$ diffuse members --methods my.apk

$ diffuse members --aar --fields my.aar
```

See more information about the subcommands and their options/arguments by running with `--help`.


## Install

**Mac OS**

```
$ brew install JakeWharton/repo/diffuse
```

**Other**

Download ZIP from
[latest release](https://github.com/JakeWharton/diffuse/releases/latest).


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
