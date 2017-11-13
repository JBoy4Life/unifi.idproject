$bootstrap_script = <<BOOTSTRAP
#!/bin/sh
set -eu

# Add unoffocial Oracle JDK repo
apt-get -y install software-properties-common dirmngr
apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886
add-apt-repository "deb http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main"
echo "oracle-java9-installer shared/accepted-oracle-license-v1-1 select true" |\
  debconf-set-selections

# Install everything
apt-get -y update
apt-get -y upgrade
apt-get -y install oracle-java9-installer oracle-java9-set-default \
                   oracle-java9-unlimited-jce-policy postgresql redis-server \
                   git maven net-tools cfengine3 nginx

# Set up DB
for hba_file in /etc/postgresql/*/main/pg_hba.conf; do
  echo "local all all trust\nhost all all 0.0.0.0/0 trust" > "$hba_file"
done
sudo systemctl reload postgresql
sudo -u postgres createuser -s vagrant
sudo -u vagrant createdb core

sudo -u vagrant ln -sf /vagrant ~vagrant/unifi.id
BOOTSTRAP

$post_up_msg = <<MSG
unifi.id services development VM
================================

Now run `vagrant ssh` to get into the VM, then:

    cd unifi.id
    mvn clean install
MSG

Vagrant.configure("2") do |config|
  config.vm.box = "debian/stretch64"
  config.vm.hostname = "unifi-services.box"
  config.vm.provision :shell, :inline => $bootstrap_script
  config.vm.post_up_message = $post_up_msg
end
