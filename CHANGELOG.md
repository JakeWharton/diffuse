# Change Log

## [Unreleased]
[Unreleased]: https://github.com/JakeWharton/diffuse/compare/0.3.0...HEAD


## [0.3.0] - 2024-02-20
[0.3.0]: https://github.com/JakeWharton/diffuse/releases/tag/0.3.0

**Added**
- Support `.dex` as an input type with `--dex` flag.

**Fixed**
- Ensure empty line appears between summary table and diff items.


## [0.2.0] - 2024-02-13
[0.2.0]: https://github.com/JakeWharton/diffuse/releases/tag/0.2.0

**Added**
- `.aab` base module now displays a diff.
- Display v4 signatures.

**Changed**
- Hide trailing numbers from lambda methods to attempt to clean up diffs.
- Update internal ASM library to handle newer Java bytecode versions.
- Update internal binary-resources library to handle empty `.arsc` resource tables.
- Migrated the CLI to the Gradle application plugin. This produces a `.zip` instead of an executable `.jar`.

**Fixed**
- Handle comments in R8 mapping files which are indented with whitespace.
- Apply R8 mapping file before displaying a dex file diff.
- Support reading zip entry sizes when the contents were streamed and thus use a trailing data descriptor. This also allows more accurately reporting the zip storage size of entries.
- Handle the same XML namespace URL being mapped to two different namespace names.


## [0.1.0] - 2020-08-27
[0.1.0]: https://github.com/JakeWharton/diffuse/releases/tag/0.1.0

Initial release

----

`dex-member-list` Change Log:
=============================

Note: The project was renamed from `dex-member-list` to `diffuse` at this point in the version
history. The versions below are for the `dex-member-list` tool.


Version 4.1.1 *(2019-09-11)*
----------------------------

 * Fix: Do not crash on inputs that have no code.


Version 4.1.0 *(2019-08-31)*
----------------------------

 * New: Add support for desugaring language features and API levels based on a target minimum API
   level. This requires supplying library jars such as `android.jar` or `rt.jar`.
 * New: Expose declared and referenced methods separately. The former always impact the final APK
   whereas the latter are a shared cost that is de-duplicated across all libraries.
 * New: Update D8 dex compiler to v1.5.x.
 * Fix: Support dexing jar, aar, and class inputs which produce more than one dex file.


Version 4.0.0 *(2019-08-08)*
----------------------------

  * New: Support for mapping files produced by R8 or ProGuard. Dump obfuscated APKs in unobfuscated
    form by providing this file on the command line or in the API using `ApiMapping`.
  * New: Kotlin users can create a `DexParser` using extension functions on receiver types
    (like `File`, `Path`, etc.) instead of Java-like static factories.
  * New: Types are modeled using a new `TypeDescriptor` class.
  * New: Add `dexCount()` method to `DexParser`.
  * Remove support for using `dx` as a dex compiler for class files.
  * Remove old `dex-field-list` and `dex-method-list` binaries and types.


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
