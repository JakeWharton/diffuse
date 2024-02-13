# Releasing

1. Update the `VERSION_NAME` in `gradle.properties` to the release version.

2. Update the `CHANGELOG.md`:
   1. Change the `Unreleased` header to the release version.
   2. Add a link URL to ensure the header link works.
   3. Add a new `Unreleased` section to the top.

3. Commit

   ```
   $ git commit -am "Prepare version X.Y.X"
   ```

4. Publish

    ```
    $ ./gradlew clean publish
    ```

    If this fails, fix, commit, and repeat.
    You may also have to drop the Sonatype repository.

5. Tag

   ```
   $ git tag -am "Version X.Y.Z" X.Y.Z
   ```

6. Update the `VERSION_NAME` in `gradle.properties` to the next "SNAPSHOT" version.

7. Commit

   ```
   $ git commit -am "Prepare next development version"
   ```

8. Push!

   ```
   $ git push && git push --tags
   ```

   This will trigger a GitHub Action workflow which will create a GitHub release with the
   change log and binary, and send a PR to the Homebrew repo.

9. Find [the Homebrew PR](https://github.com/JakeWharton/homebrew-repo/pulls) and merge it!
