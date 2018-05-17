variable "aws_access_key" {}
variable "aws_secret_key" {}

##################################################################
# Environment specific, defined in tfvars for that environment
##################################################################

variable "aws_vpc_description" {
  description = "Name for the whole VPC"

  #default     = "10.0.0.0/16"
}

variable "aws_vpc_cidr" {
  description = "CIDR for the whole VPC"

  #default     = "10.0.0.0/16"
}

variable "aws_subnet_cidr" {
  description = "CIDR for the whole VPC"

  #default     = "10.0.0.0/24"
}

variable "unifi_nodeid" {
  description = "Node ID of this Unifi.id structure"

  #default     = "test"
}

variable "unifi_environment" {
  description = "Environment type (local, dev, uat, sit, prod)"

  #default     = "dev"
}

variable "unifi_env_name" {
  description = "Environment name (a general identifier)"

  #default     = "dev"
}

variable "aws_public_key_path" {
  description = <<DESCRIPTION
Path to the SSH public key to be used for authentication.
Ensure this keypair is added to your local SSH agent so provisioners can
connect.

Example: ~/.ssh/terraform.pub
DESCRIPTION

  #  default = "ssh/id_rsa.pub"
}

variable "aws_private_key_path" {
  description = <<DESCRIPTION
Path to the SSH public key to be used for authentication.
Ensure this keypair is added to your local SSH agent so provisioners can
connect.

Example: ~/.ssh/terraform.pub
DESCRIPTION

  #  default = "ssh/id_rsa"
}

variable "aws_key_name" {
  description = "Desired name of AWS key pair"

  #  default     = "Unifi-Terraform"
}

variable "aws_region" {
  description = "AWS region to launch servers."

  #default     = "eu-west-1"
}

variable "postgres_password" {
  description = "Secure root/admin password to pass to RDS Postgres instances."

  #default     = "eu-west-1"
}

########################################################
# Defined Variables
########################################################

variable "aws_amis" {
  description = "AMIs by region"

  default = {
    # debian stretch
    eu-west-3 = "ami-6609bf1b"
    eu-west-2 = "ami-9ebd5df9"
    eu-west-1 = "ami-08025971"
  }
}

variable "ssh_access_list" {
  description = "List of Network CIDR's that should have SSH access to the instances"
  default     = ["111.220.76.134/32"]
}

variable "ec2_instance_ips" {
  default = {
    policy-server = "10.10.1.10"
    app           = "10.10.1.11"
    services      = "10.10.1.12"
    db            = "10.10.1.13"
    agent         = "10.10.1.14"
  }
}

variable "ec2_instance_sizes" {
  default = {
    policy-server = "t2.small"
    app           = "m4.large"

    #policy-server = "t2.micro"
    #app = "t2.micro"
  }
}

variable "rds_instance_sizes" {
  default = {
    db = "t2.large"
  }
}
