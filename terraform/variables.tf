variable "node_id" {
  description = "Node ID of this Unifi.id structure"
  default     = "test"
}

variable "environment" {
  description = "Environment type (local, dev, uat, sit, prod)"
  default     = "dev"
}

variable "public_key_path" {
  description = <<DESCRIPTION
Path to the SSH public key to be used for authentication.
Ensure this keypair is added to your local SSH agent so provisioners can
connect.

Example: ~/.ssh/terraform.pub
DESCRIPTION

  default = "ssh/id_rsa.pub"
}

variable "private_key_path" {
  description = <<DESCRIPTION
Path to the SSH public key to be used for authentication.
Ensure this keypair is added to your local SSH agent so provisioners can
connect.

Example: ~/.ssh/terraform.pub
DESCRIPTION

  default = "ssh/id_rsa"
}

variable "key_name" {
  description = "Desired name of AWS key pair"
  default     = "Unifi-Terraform"
}

variable "aws_region" {
  description = "AWS region to launch servers."
  default     = "eu-west-1"
}

#variable "aws_small" {}

# Ubuntu Precise 12.04 LTS (x64)
variable "aws_amis" {
  default = {
    ap-southeast-2 = "ami-b362a2d1"
    eu-central-1   = "ami-6ef69f01"
    eu-west-1      = "ami-044b047d"
    eu-west-2      = "ami-8702e5e0"
    eu-west-3      = "ami-055fe978"
    us-east-1      = "ami-1d4e7a66"
    us-west-1      = "ami-969ab1f6"
    us-west-2      = "ami-8803e0f0"
  }
}

variable "vpc_cidr" {
  description = "Network CIDR for the whole of the VPC"
  default     = "10.0.0.0/16"
}

variable "subnet_cidr" {
  description = "Network CIDR for the subnet"
  default     = "10.0.80.0/24"
}

variable "ssh_access_list" {
  description = "List of Network CIDR's that should have SSH access to the instances"
  default     = ["111.220.76.134/32"]
}

variable "ec2_instance_ips" {
  default = {
    policy-server = "10.0.80.10"
    app           = "10.0.80.11"
    services      = "10.0.80.12"
    db            = "10.0.80.13"
    agent         = "10.0.80.14"
  }
}

variable "ec2_instance_sizes" {
  default = {
    policy-server = "t2.nano"
    app           = "t2.nano"
    services      = "t2.nano"
    db            = "t2.nano"
    agent         = "t2.nano"
  }
}
