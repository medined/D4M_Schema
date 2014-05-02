master_node_name = "affy-master"
slave1_node_name = "affy-slave1"
slave2_node_name = "affy-slave2"

Vagrant.configure("2") do |config|

  config.hostmanager.enabled = true
  config.hostmanager.manage_host = true
  config.hostmanager.ignore_private_ip = false
  config.hostmanager.include_offline = true
  config.ssh.forward_x11 = true

  config.vm.define :master do |master|
    master.vm.box = "affy-master"
    master.vm.provider :virtualbox do |v|
      v.name = master_node_name
      v.customize ["modifyvm", :id, "--memory", "3000"]
    end
    master.vm.network :private_network, ip: "10.211.55.100"
    master.vm.hostname = master_node_name
  end

  config.vm.define :slave1 do |slave1|
    slave1.vm.box = "affy-slave1"
    slave1.vm.provider :virtualbox do |v|
      v.name = slave1_node_name
      v.customize ["modifyvm", :id, "--memory", "1024"]
    end
    slave1.vm.network :private_network, ip: "10.211.55.101"
    slave1.vm.hostname = slave1_node_name
  end

  config.vm.define :slave2 do |slave2|
    slave2.vm.box = "affy-d4m"
    slave2.vm.provider :virtualbox do |v|
      v.name = slave2_node_name
      v.customize ["modifyvm", :id, "--memory", "4096"]
    end
    slave2.vm.network :private_network, ip: "10.211.55.102"
    slave2.vm.hostname = slave2_node_name
  end

end

