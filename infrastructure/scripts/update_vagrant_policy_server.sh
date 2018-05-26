#!/bin/bash

sudo rsync -vrlpxE /vagrant/deployment/masterfiles/* /var/cfengine/masterfiles/ && sudo cf-agent -Kf /var/cfengine/inputs/failsafe.cf
