$bootstrap_script = <<BOOTSTRAP
#!/bin/sh
set -eu

<<<<<<< HEAD
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

=======
>>>>>>> origin/cfengine-deployment
# Sorry.
# apt-get -y install curl
# curl -sL https://deb.nodesource.com/setup_8.x | bash -
# curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
# echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list
# apt-get update && apt-get -y install yarn

sudo -u vagrant ln -sf /vagrant ~vagrant/unifi.id
BOOTSTRAP

$post_up_msg = <<MSG
= unifi.id local development environment

There are five node roles available:

* policy-server: hosts CFEngine configuration.
* app: serves web application and other resources.
* services: backend services.
* db: transactional database.
* agent: on-premises agent.

You may `vagrant ssh <role>` to access any of these.

Note that all configuration is done via CFEngine. To change what is
installed on a particular node:

. Edit `deployment/services/autorun/unifi.cf` in the project folder.
. `vagrant ssh policy-server`
. `cp /vagrant/**/unifi.cf /var/cfengine/masterfiles/service/autorun/unifi.cf`
. `cd /var/cfengine/inputs`
. `cf-agent -Kf failsafe.cf`
. Log out.

Alternatively:
. Edit `deployment/services/autorun/unifi.cf` in the project folder.
. `vagrant ssh policy-server`
. `/vagrant/scripts/update_vagrant_policy_server.sh`

   All other nodes will pick up the new policy and apply it within a few
   minutes, but if you want to force it:

. `vagrant ssh <role>`
. `cd /var/cfengine/inputs`
. `cf-agent -Kf failsafe.cf && cf-agent -v -D unifi_role_<role> -Kf promises.cf`
.  Repeat until 100% of promises are kept.

MSG

Vagrant.configure("2") do |config|
<<<<<<< HEAD
  config.vm.box = "debian/contrib-stretch64" # contrib includes vbox guest additions
  config.vm.synced_folder ".", "/vagrant", type: "virtualbox"
  config.vm.hostname = "unifi-services.box"
  config.vm.provision :shell, :inline => $bootstrap_script

  # Sometimes, bridging to the public network does not acquire an IPv4 address.
  # Forwarding ports will always work.
  config.vm.network "forwarded_port", guest: 3000, host: 3000 # unifi-web
  config.vm.network "forwarded_port", guest: 8000, host: 8000 # unifi-service

=======

  config.vm.box = "generic/debian9"
  config.vm.synced_folder ".", "/vagrant"
>>>>>>> origin/cfengine-deployment
  config.vm.post_up_message = $post_up_msg

  config.vm.define "policy-server" do |ps|
    ps.vm.provision :shell, :inline => $bootstrap_script
    ps.vm.provision "cfengine" do |cf|
        cf.am_policy_hub = true
        cf.policy_server_address = "10.0.80.10"
        cf.files_path = "deployment"
    end
    ps.vm.hostname = "a.policy-server.local.unifi.id"
    ps.vm.network "private_network", ip: "10.0.80.10"
    ps.vm.network "public_network"
  end

  config.vm.define "app" do |app|
    app.vm.provision :shell, :inline => $bootstrap_script
    app.vm.provision "cfengine" do |cf|
        cf.policy_server_address = "10.0.80.10"
    end
    app.vm.hostname = "a.app.local.unifi.id"
    app.vm.network "private_network", ip: "10.0.80.11"
    app.vm.network "public_network"
  end

  config.vm.define "services" do |services|
    services.vm.provision :shell, :inline => $bootstrap_script
    services.vm.provision "cfengine" do |cf|
        cf.policy_server_address = "10.0.80.10"
    end
    services.vm.hostname = "a.services.local.unifi.id"
    services.vm.network "private_network", ip: "10.0.80.12"
    services.vm.network "public_network"
  end

  config.vm.define "db" do |db|
    db.vm.provision :shell, :inline => $bootstrap_script
    db.vm.provision "cfengine" do |cf|
        cf.policy_server_address = "10.0.80.10"
    end
    db.vm.hostname = "a.db.local.unifi.id"
    db.vm.network "private_network", ip: "10.0.80.13"
    db.vm.network "public_network"
  end

  config.vm.define "agent" do |agent|
    agent.vm.provision :shell, :inline => $bootstrap_script
    agent.vm.provision "cfengine" do |cf|
        cf.policy_server_address = "10.0.80.10"
    end
    agent.vm.hostname = "a.agent.local.unifi.id"
    agent.vm.network "private_network", ip: "10.0.80.14"
    agent.vm.network "public_network"
  end

  config.vm.define "buildnode" do |buildnode|
    buildnode.vm.provision :shell, :inline => $bootstrap_script
    buildnode.vm.provision "cfengine" do |cf|
        cf.policy_server_address = "10.0.80.10"
    end
    buildnode.vm.hostname = "a.buildnode.local.unifi.id"
    buildnode.vm.network "private_network", ip: "10.0.80.50"
    buildnode.vm.network "public_network"
  end

  config.vm.define "allinone" do |allinone|
    allinone.vm.provision :shell, :inline => $bootstrap_script
    allinone.vm.provision "cfengine" do |cf|
        cf.policy_server_address = "10.0.80.10"
    end
    allinone.vm.hostname = "a.allinone.local.unifi.id"
    allinone.vm.network "private_network", ip: "10.0.80.100"
    allinone.vm.network "public_network"
  end

  config.vm.define "all-in-one" do |aio|
    aio.vm.provision :shell, :inline => $bootstrap_script
    aio.vm.provision "cfengine" do |cf|
        cf.am_policy_hub = true
        cf.policy_server_address = "localhost"
        cf.files_path = "deployment"
    end
    aio.vm.hostname = "a.all-in-one.local.unifi.id"
    aio.vm.network "private_network", ip: "10.0.80.60"
    aio.vm.network "public_network"
  end
end
