$bootstrap_script = <<BOOTSTRAP
#!/bin/sh
set -eu

# Add unoffocial Oracle JDK repo
apt-get -y install software-properties-common dirmngr
apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 73C3DB2A

echo "deb http://ppa.launchpad.net/linuxuprising/java/ubuntu xenial main " |\
  tee -a /etc/apt/sources.list.d/linux-uprising_java.list
echo "deb http://ppa.launchpad.net/linuxuprising/java/ubuntu xenial main " |\
  tee -a /etc/apt/sources.list.d/linux-uprising_java.list
echo "oracle-java10-installer shared/accepted-oracle-license-v1-1 select true" |\
  debconf-set-selections

# Install everything
apt-get -y update
apt-get -y upgrade
apt-get -y install oracle-java10-installer oracle-java10-set-default git \
                   postgresql maven net-tools cfengine3 nginx rabbitmq-server

# Sorry.
apt-get -y install curl
curl -sL https://deb.nodesource.com/setup_8.x | bash -
curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list
apt-get update && apt-get -y install yarn

# Set up DB
for hba_file in /etc/postgresql/*/main/pg_hba.conf; do
  echo "local all all trust\nhost all all 0.0.0.0/0 trust" > "$hba_file"
done
sudo systemctl reload postgresql
sudo -u postgres createuser -s vagrant || true
sudo -u vagrant dropdb --if-exists unifi
sudo -u vagrant createdb unifi

sudo -u vagrant ln -sf /vagrant ~vagrant/unifi.id
BOOTSTRAP

$post_up_msg = <<MSG
unifi.id services development VM
================================

Now run `vagrant ssh` to get into the VM, then:

    cd unifi.id/unifi-service
    mvn clean install
MSG

Vagrant.configure("2") do |config|
  config.vm.box = "debian/contrib-stretch64" # contrib includes vbox guest additions
  config.vm.synced_folder ".", "/vagrant", type: "virtualbox"
  config.vm.hostname = "unifi-services.box"
  config.vm.provision :shell, :inline => $bootstrap_script

  # Sometimes, bridging to the public network does not acquire an IPv4 address.
  # Forwarding ports will always work.
  config.vm.network "forwarded_port", guest: 3000, host: 3000 # unifi-web
  config.vm.network "forwarded_port", guest: 8000, host: 8000 # unifi-service

  config.vm.post_up_message = $post_up_msg
end
