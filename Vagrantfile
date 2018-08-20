$preboot_script = <<PREBOOT
#!/bin/sh
set -eu

# Basic hygiene.
apt-get -y update
apt-get -y upgrade
apt-get -y install curl rsync

# Get CFEngine from the official site, not the Debian repository.
curl -O https://cfengine-package-repos.s3.amazonaws.com/community_binaries/Community-3.10.4/agent_deb_x86_64/cfengine-community_3.10.4-1_amd64-debian4.deb
sudo dpkg -i cfengine-community*.deb

# Set up the environment.
sudo echo "DefaultEnvironment=UNIFI_ENV=local" >> /etc/systemd/system.conf
sudo echo "DefaultEnvironment=UNIFI_ROLES=app,services,agent,db" >> /etc/systemd/system.conf
sudo echo "DefaultEnvironment=UNIFI_NODEID=vagrant" >> /etc/systemd/system.conf
sudo echo "DefaultEnvironment=UNIFI_CLIENTID=test-club" >> /etc/systemd/system.conf
sudo echo "DefaultEnvironment=UNIFI_PAPERTRAIL_HOST=logs5.papertrail.com" >> /etc/systemd/system.conf
sudo echo "DefaultEnvironment=UNIFI_PAPERTRAIL_PORT=12345" >> /etc/systemd/system.conf
sudo echo "DefaultEnvironment=UNIFI_APPOPTICS_APIKEY=cafebabecbeafbeabfaefbaebcdcbdc329842893" >> /etc/systemd/system.conf
sudo echo "DefaultEnvironment=UNIFI_CORE_JDBC_URL=jdbc:postgresql://localhost/unifi" >> /etc/systemd/system.conf
sudo echo "DefaultEnvironment=UNIFI_CORE_JDBC_USER=vagrant" >> /etc/systemd/system.conf
sudo echo "DefaultEnvironment=UNIFI_SMS_ENABLED=true" >> /etc/systemd/system.conf
sudo echo "DefaultEnvironment=UNIFI_SMS_AWS_REGION=eu-west-1" >> /etc/systemd/system.conf

sudo echo "UNIFI_ENV=local" >> /etc/environment
sudo echo "UNIFI_ROLES=app,services,agent,db" >> /etc/environment
sudo echo "UNIFI_NODEID=vagrant" >> /etc/environment
sudo echo "UNIFI_CLIENTID=test-club" >> /etc/environment
sudo echo "UNIFI_PAPERTRAIL_HOST=logs5.papertrail.com" >> /etc/environment
sudo echo "UNIFI_PAPERTRAIL_PORT=12345" >> /etc/environment
sudo echo "UNIFI_APPOPTICS_APIKEY=cafebabecbeafbeabfaefbaebcdcbdc329842893" >> /etc/environment
sudo echo "UNIFI_CORE_JDBC_URL=jdbc:postgresql://localhost/unifi" >> /etc/environment
sudo echo "UNIFI_CORE_JDBC_USER=vagrant" >> /etc/environment
sudo echo "UNIFI_SMS_ENABLED=true" >> /etc/environment
sudo echo "UNIFI_SMS_AWS_REGION=eu-west-1" >> /etc/environment

# Sync up the CFEngine masterfiles folder with the repo.
sudo -u vagrant ln -sf /vagrant ~vagrant/unifi.id
sudo rsync -vrlpxE /vagrant/infrastructure/deployment/masterfiles/* /var/cfengine/masterfiles/
PREBOOT

$post_up_msg = <<MSG
unifi.id services development VM
================================

Now run `vagrant ssh` to get into the VM.

You will want to bootstrap and force convergence on the machine the first time:

    sudo -i

    # Manually load the environment because sudo nukes it.
    $(cat /etc/environment | while read line; do echo "export $line"; done)

    # Bootstrap against self.
    cf-agent --bootstrap <vm_network_ip>

    # Keep running the below until you see 100% convergence.
    cf-agent -Kf failsafe.cf && cf-agent -vKf promises.cf

Any time you modify CFE stuff, you can rsync and repeated-converge again.

Go forth and develop!

MSG

Vagrant.configure("2") do |config|
  config.vm.box = "generic/debian9"
  config.vm.synced_folder ".", "/vagrant"
  config.vm.hostname = "unifi-services.box"
  config.vm.provision :shell, :inline => $preboot_script
  # config.vm.provision :reload
  # TODO: can we force-converge in a postboot script?

  # Sometimes, bridging to the public network does not acquire an IPv4 address.
  # Forwarding ports will always work.
  config.vm.network "public_network"
  config.vm.network "forwarded_port", guest: 3000, host: 3000 # unifi-web
  config.vm.network "forwarded_port", guest: 8000, host: 8000 # unifi-service

  config.vm.post_up_message = $post_up_msg
end
