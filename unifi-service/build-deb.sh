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
Description: Some description
"
}

if [ -z $1 ]; then
 usage

else 
  case $1 in
    unifi-core | unifi-agent )
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

cp -aR $source_template $destination_output
generate_control_file $application $version > $destination_output/debian/control
cp $target $destination_output/opt/unifi/unifi-core.jar

dpkg-deb --build $destination_output $application-$version.deb

rm -rf $destination_output
