$preboot_script = <<PREBOOT
#!/bin/sh
set -eu

# Basic hygiene.
apt-get -y update
apt-get -y upgrade
apt-get -y install apt-transport-https

# Get CFEngine from the official repository.
wget -qO- https://cfengine-package-repos.s3.amazonaws.com/pub/gpg.key | apt-key add -
echo "deb https://cfengine-package-repos.s3.amazonaws.com/pub/apt/packages stable main" > /etc/apt/sources.list.d/cfengine-community.list
apt-get -y update
apt-get -y install rsync cfengine-community

# Set up the environment.
echo "DefaultEnvironment=UNIFI_ENV=local" >> /etc/systemd/system.conf
echo "DefaultEnvironment=UNIFI_ROLES=app,services,agent,db" >> /etc/systemd/system.conf
echo "DefaultEnvironment=UNIFI_NODEID=vagrant" >> /etc/systemd/system.conf
echo "DefaultEnvironment=UNIFI_CLIENT_ID=test-club" >> /etc/systemd/system.conf
cat /etc/systemd/system.conf | grep UNIFI | cut -d "=" -f 2- > /etc/environment
cat /etc/systemd/system.conf | grep UNIFI | sed -e "s/DefaultEnvironment=/export /g" >> /etc/profile

# Sync up the CFEngine masterfiles folder with the repo.
sudo -u vagrant ln -sf /vagrant ~vagrant/unifi.id
rsync -vrlpxE /vagrant/infrastructure/deployment/masterfiles/* /var/cfengine/masterfiles/
cf-agent -B $(ip -o addr show eth0 | cut -d " " -f 7 | cut -d "/" -f 1)
cf-agent -Kf failsafe.cf
PREBOOT

$post_up_msg = <<MSG
unifi.id development VM
=======================

Now run `vagrant ssh` to get into the VM.

Keep running the below as `sudo -i` until you see 100% convergence.
    
    cf-agent -vKf promises.cf | grep compliance

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

  # Host-to-guest networking.
  # Your host: 172.16.0.1; the VM: 172.16.0.2.
  config.vm.network "private_network", ip: "172.16.0.2"

  # Interface that DHCPs to whatever router is on your network.
  # Generally used for outbound Internet access.
  config.vm.network "public_network"

  config.vm.post_up_message = $post_up_msg
end
