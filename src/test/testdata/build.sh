#!/usr/bin/env bash

set -ex

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

for folder in `find "$DIR" -type d -mindepth 1 -maxdepth 1`; do
  name=`basename $folder`
  pushd "$folder"
  javac *.java
  zip -r "$folder.jar" *.class
  dx --dex --output="$folder.dex" "$folder.jar"
  cp "$folder.dex" "../../resources/"
  popd
done
