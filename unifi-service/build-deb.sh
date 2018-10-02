#!/bin/bash
set -euo pipefail

function usage {
  echo  "
#####################################################################################################
#
# build-deb.sh usage
# ./build-deb.sh <package> <version> <target_jar>
#
# e.g.
#  ./build-deb.sh unifi-core 1.0.1 unifi-core/target/unifi-core-1.0.1-SNAPSHOT-jar-with-dependencies.jar
#
######################################################################################################
"
  exit 1
}

if [ -z "$1" ]; then
 usage

else 
  case "$1" in
    unifi-core )
      description="Unifi Core Service"
      application=$1
      dependencies="oracle-java10-installer (>=10.0.0), adduser"
      ;;

    unifi-core-agent )
      description="Unifi Core Agent"
      dependencies="oracle-java10-installer (>=10.0.0), adduser"
      application=$1
      ;;

    *)
      echo -en "\nMissing Application Type\n"
      usage
      ;; 
  esac

  if [ -z "$2" ]; then
    echo -en "\nMissing Version\n"
    usage
  fi

  if [ ! -f "$3" ]; then
    echo -en "\nInvalid file\n"
    usage
  fi
fi

application=$1
version=$2
target=$3

source_template=debian/$application-TEMPLATE
destination_output=debian/${application}_$version

mkdir -p "$destination_output/DEBIAN"
cp -aR "$source_template"/* "$destination_output"
echo "
Package: $application
Architecture: all
Maintainer: Unifi.id<info@unifi.id>
Priority: optional
Version: $version
Description: $description
Depends: $dependencies
" > "$destination_output/DEBIAN/control"

mkdir -p "$destination_output/opt/unifi"
cp "$target" "$destination_output/opt/unifi/$application.jar"
cp "misc/systemd/$application.service" "$destination_output/etc/systemd/system/$application.service"

mkdir -p target
dpkg-deb --build "$destination_output" "target/$application-$version.deb"

rm -rf "$destination_output"
