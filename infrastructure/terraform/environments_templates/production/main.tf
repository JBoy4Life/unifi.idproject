############ Adapted from the two-tier Example Recipe's on Terraform's github
# This is designed to be a 1:1 replacement for a vagrant install, even the static IP's are the same.
# All variable details are stored in variables.tf

# Specify the provider and access details
provider "aws" {
  region = "${var.aws_region}"
}

# Create a VPC to launch our instances into
resource "aws_vpc" "default" {
  cidr_block           = "${var.aws_vpc_cidr}"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags {
    Name = "terraform-aws-vpc"
  }
}

# Create an internet gateway to give our subnet access to the outside world
resource "aws_internet_gateway" "default" {
  vpc_id = "${aws_vpc.default.id}"
}

# Grant the VPC internet access on its main route table
resource "aws_route" "internet_access" {
  route_table_id         = "${aws_vpc.default.main_route_table_id}"
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = "${aws_internet_gateway.default.id}"
}

# Create a subnet to launch our instances into
resource "aws_subnet" "default" {
  vpc_id                  = "${aws_vpc.default.id}"
  cidr_block              = "${var.aws_subnet_cidr}"
  map_public_ip_on_launch = true
}

/*
# A security group for the ELB so it is accessible via the web
resource "aws_security_group" "elb" {
  name        = "terraform_example_elb"
  description = "Used in the terraform"
  vpc_id      = "${aws_vpc.default.id}"

  # HTTP access from anywhere
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # outbound internet access
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
*/

# Our default security group to access
# the instances over SSH and HTTP

resource "aws_security_group" "terraform_default" {
  name        = "terraform_default"
  description = "Used in the terraform"
  vpc_id      = "${aws_vpc.default.id}"

  # SSH access from defined sources
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = "${var.ssh_access_list}"
  }

  # HTTP access from the VPC
  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["${var.aws_subnet_cidr}"]
  }

  # outbound internet access
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

/*
resource "aws_elb" "web" {
  name = "terraform-example-elb"

  subnets         = ["${aws_subnet.default.id}"]
  security_groups = ["${aws_security_group.elb.id}"]
  instances       = ["${aws_instance.web.id}"]

  listener {
    instance_port     = 80
    instance_protocol = "http"
    lb_port           = 80
    lb_protocol       = "http"
  }
}
*/

resource "aws_db_instance" "default" {
  allocated_storage         = 100
  storage_type              = "gp2"
  engine                    = "postgres"
  engine_version            = "10.3"
  instance_class            = "db.t2.medium"
  name                      = "unifi"
  username                  = "unifi"
  password                  = "${var.postgres_password}"
  final_snapshot_identifier = "${var.aws_env_name}-final-snapshot-${md5(timestamp())}"

  #  parameter_group_name = "default.postres10.3"
  multi_az = true
}

resource "aws_key_pair" "auth" {
  key_name   = "${var.aws_key_name}"
  public_key = "${file(var.aws_public_key_path)}"
}

resource "aws_network_interface" "nic-policy-server" {
  subnet_id   = "${aws_subnet.default.id}"
  private_ips = ["${lookup(var.ec2_instance_ips, "policy-server")}"]

  #vpc_security_group_ids = ["${aws_security_group.terraform_default.id}"]
  security_groups = ["${aws_security_group.terraform_default.id}"]

  tags {
    Name = "primary_network_interface"
  }
}

resource "aws_instance" "policy-server" {
  # The connection block tells our provisioner how to
  # communicate with the resource (instance)
  connection {
    # The default username for our AMI
    user = "admin"

    # The connection will use the local SSH agent for authentication.
    private_key = "${file(var.aws_private_key_path)}"
  }

  instance_type = "${lookup(var.ec2_instance_sizes, "policy-server")}"

  # Lookup the correct AMI based on the region
  # we specified
  ami = "${lookup(var.aws_amis, var.aws_region)}"

  # The name of our SSH keypair we created above.
  key_name = "${aws_key_pair.auth.id}"

  # Our Security group to allow HTTP and SSH access
  #vpc_security_group_ids = ["${aws_security_group.terraform_default.id}"]

  # We're going to launch into the same subnet as our ELB. In a production
  # environment it's more common to have a separate private subnet for
  # backend instances.
  #subnet_id = "${aws_subnet.default.id}"
  network_interface {
    network_interface_id = "${aws_network_interface.nic-policy-server.id}"
    device_index         = 0
  }
  # We run a remote provisioner on the instance after creating it.
  # In this case, we just install nginx and start it. By default,
  # this should be on port 80
  provisioner "file" {
    source      = "../../../deployment/masterfiles"
    destination = "/home/admin/masterfiles"
  }
  provisioner "remote-exec" {
    inline = [
      "sudo hostnamectl set-hostname ${var.unifi_nodeid}.policy-server.${var.unifi_environment}.unifi.id",
      "sudo sh -c 'echo \"${lookup(var.ec2_instance_ips, "policy-server")} ${var.unifi_nodeid}.policy-server.${var.unifi_environment}.unifi.id\" >> /etc/hosts'",
      "sudo sh -c 'wget -qO- https://cfengine-package-repos.s3.amazonaws.com/pub/gpg.key | apt-key add -'",
      "sudo sh -c 'echo \"deb https://cfengine-package-repos.s3.amazonaws.com/pub/apt/packages stable main\" > /etc/apt/sources.list.d/cfengine-community.list'",
      "sudo apt-get update && sudo apt-get install -y cfengine-community rsync",
      "sudo /var/cfengine/bin/cf-agent --bootstrap ${lookup(var.ec2_instance_ips, "policy-server")}",
      "sudo rsync -vaxE /home/admin/masterfiles/* /var/cfengine/masterfiles",
    ]
  }
  tags {
    Name = "${var.unifi_nodeid}.policy-server.${var.unifi_environment}.unifi.id"
  }
}

resource "aws_network_interface" "nic-app" {
  subnet_id = "${aws_subnet.default.id}"

  #private_ips = ["10.0.80.11"]
  private_ips = ["${lookup(var.ec2_instance_ips, "app")}"]

  #vpc_security_group_ids = ["${aws_security_group.terraform_default.id}"]
  security_groups = ["${aws_security_group.terraform_default.id}"]

  tags {
    Name = "primary_network_interface"
  }
}

resource "aws_instance" "app" {
  # The connection block tells our provisioner how to
  # communicate with the resource (instance)
  connection {
    # The default username for our AMI
    user = "admin"

    # The connection will use the local SSH agent for authentication.
    private_key = "${file(var.aws_private_key_path)}"
  }

  #instance_type = "t2.nano"
  instance_type = "${lookup(var.ec2_instance_sizes, "app")}"

  # Lookup the correct AMI based on the region
  # we specified
  ami = "${lookup(var.aws_amis, var.aws_region)}"

  # The name of our SSH keypair we created above.
  key_name = "${aws_key_pair.auth.id}"

  # Our Security group to allow HTTP and SSH access
  #vpc_security_group_ids = ["${aws_security_group.terraform_default.id}"]

  # We're going to launch into the same subnet as our ELB. In a production
  # environment it's more common to have a separate private subnet for
  # backend instances.
  #subnet_id = "${aws_subnet.default.id}"
  network_interface {
    network_interface_id = "${aws_network_interface.nic-app.id}"
    device_index         = 0
  }
  # We run a remote provisioner on the instance after creating it.
  # In this case, we just install nginx and start it. By default,
  # this should be on port 80
  provisioner "remote-exec" {
    inline = [
      "sudo hostnamectl set-hostname ${var.unifi_nodeid}.app.${var.unifi_environment}.unifi.id",
      "sudo sh -c 'echo \"${lookup(var.ec2_instance_ips, "app")} ${var.unifi_nodeid}.app.${var.unifi_environment}.unifi.id\" >> /etc/hosts'",
      "sudo sh -c 'wget -qO- https://cfengine-package-repos.s3.amazonaws.com/pub/gpg.key | apt-key add -'",
      "sudo sh -c 'echo \"deb https://cfengine-package-repos.s3.amazonaws.com/pub/apt/packages stable main\" > /etc/apt/sources.list.d/cfengine-community.list'",
      "sudo apt-get update && sudo apt-get install cfengine-community",
      "sudo /var/cfengine/bin/cf-agent --bootstrap ${lookup(var.ec2_instance_ips, "policy-server")}",
    ]
  }
  tags {
    Name = "${var.unifi_nodeid}.app.${var.unifi_environment}.unifi.id"
  }
  depends_on = ["aws_instance.policy-server"]
}
