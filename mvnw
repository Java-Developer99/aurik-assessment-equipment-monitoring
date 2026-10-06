#!/bin/sh
MVN_VERSION=3.9.11
BASE="$HOME/.m2/wrapper/dists/apache-maven-$MVN_VERSION"
HOME_MVN="$BASE/apache-maven-$MVN_VERSION"
if [ ! -x "$HOME_MVN/bin/mvn" ]; then
  echo "Maven $MVN_VERSION not found. Downloading..."
  mkdir -p "$BASE"
  curl -fL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MVN_VERSION/apache-maven-$MVN_VERSION-bin.tar.gz" -o "$BASE/maven.tar.gz"
  tar -xzf "$BASE/maven.tar.gz" -C "$BASE"
  rm -f "$BASE/maven.tar.gz"
fi
exec "$HOME_MVN/bin/mvn" "$@"
