Change Log
==========

Version 3.3.0 *(2019-03-28)*
----------------------------

 * New: Remove synthetic numbers from Kotlin lambda types and lambda functions.
 * Fix: De-duplicate method and field instances when multiple inputs are supplied. This means that
   the output for APKs with multiple DEX files, for example, will output the total unique list of
   referenced methods. This number will be smaller than the sum of the referenced methods from the
   DEX files, but it more accurately reflects the count. If you want the old behavior, pass the
   DEX files separately and concatenate the lists yourself.


Version 3.2.1 *(2018-07-18)*
----------------------------

 * Fix: Binary names are now correctly singular instead of plural.


Version 3.2.0 *(2018-06-18)*
----------------------------

 * New: `DexParser.list()` method returns a list of all methods and fields as `DexMethod` and
   `DexField` now both extend `DexMember`.
 * New: `dex-members-list` binary lists all members. Supply `--methods` or `--fields` to filter
   to members of one type.
 * New: Artifact ID has changed to `dex-member-list`.


Version 3.1.0 *(2018-04-21)*
----------------------------

 * New: D8 compiler is now the default. Pass `--legacy-dx` to use DX compiler from the command line.
 * New: `DexParser` class and factory methods replace `DexMethods.list`/`DexFields.list` (Java) and `dexMethods`/`dexFields` (Kotlin).


Version 3.0.0 *(2018-03-05)*
----------------------------

 * New: `dex-fields-list` command for listing field references.
 * New: Added `DexFields` and `DexField` types for listing field references.
 * Fix: Ignore `META-INF` contents.

Note: The dx and dex dependencies are no longer shadowed in the jar dependency.


Version 2.0.0 *(2017-10-12)*
----------------------------

 * New: Show non-`void` return types.
 * New: API now returns a model object representing methods.
 * The Dalvik and Dex dependencies are now shaded to prevent conflicts in some environments.


Version 1.2.0 *(2016-02-28)*
----------------------------

 * New: Support for reading the `.jar` inside of `.aar` files.
 * New: Add `--hide-synthetic-numbers` flag for removing the number suffix from synthetic methods.


Version 1.1.0 *(2016-02-04)*
----------------------------

 * New: Support for `.class` and `.jar` files!


Version 1.0.0 *(2016-01-02)*
----------------------------

Initial release.
