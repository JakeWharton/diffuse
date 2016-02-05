#!/usr/bin/env bash

set -ex

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR

pushd ../resources
rm -f *.dex
popd

for folder in `find "$DIR" -type d -mindepth 1 -maxdepth 1`; do
  name=`basename $folder`
  pushd "$folder"
  rm -f *.class
  javac *.java
  cp *.class "../../resources/"
  zip -r "$folder.jar" *.class
  rm *.class
  cp "$folder.jar" "../../resources/"
  dx --dex --output="$folder.dex" "$folder.jar"
  rm "$folder.jar"
  cp "$folder.dex" "../../resources/"
  rm "$folder.dex"
  popd
done

pushd ../resources
zip three.apk *.dex
zip one.apk params_joined.dex
zip three.jar *.class
