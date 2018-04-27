#!/bin/bash

function usage {
  echo  "
#####################################################################################################
#
# build_deb.sh usage
# ./build_deb.sh <package> <version> <target_jar>
#
# i.e
#  ./build_deb.sh unifi-core 1.0.1 unifi-core/target/unifi-core-1.0.1-SNAPSHOT-with-dependencies.jar
#
######################################################################################################
"
  exit 1
}

function generate_control_file {
  echo "
Package: $1
Architecture: all
Maintainer: @unifi
Priority: optional
Version: $2
Description: $3

Depends: $4
"
}

if [ -z $1 ]; then
 usage

else 
  case $1 in
    unifi-core )

      description="Unifi Core Daemon"
      application=$1
      dependencies="oracle-java10-installer (>=10.0.0)"
      #dependencies="oracle-java10-installer"
      ;;
    unifi-core-agent )

      description="Unifi Core Agent Daemon"
      dependenciaes="oracle-java10-installer (>=10.0.0)"
      #dependencies="oracle-java10-installer"
      application=$1
      ;;
    *)
      echo "Missing Application Type"
      usage
      ;; 
  esac

  if [ -z $2 ]; then
    echo "Missing Version"
    usage
  fi

  if [ ! -f $3 ]; then
    echo "Invalid file"
    usage
  fi
fi

application=$1
version=$2
target=$3

source_template=debian/$application-TEMPLATE
destination_output=debian/$application-$version

mkdir -p $destination_output/DEBIAN
cp -aR $source_template $destination_output
echo "
Package: $application
Architecture: all
Maintainer: @unifi
Priority: optional
Version: $version
Depends: $dependencies
Description: $description
" > $destination_output/DEBIAN/control
mkdir -p $destination_output/opt/unifi/
cp $target $destination_output/opt/unifi/unifi-core.jar

dpkg-deb --build $destination_output $application-$version.deb

rm -rf $destination_output
