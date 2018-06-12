#!/usr/bin/env bash

# save current dir before messing with it
pushd $PWD &>/dev/null

# Find the real, physical location of the script
# https://stackoverflow.com/a/246128/785663
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# Finally, position in the parent dir because comdagen expects to work from it's "home directory"
cd "$DIR/.."

# Pass on all arguments!
java -jar "${DIR}/${project.build.finalName}.jar" $@

popd &>/dev/null
