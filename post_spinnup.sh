
CNT=`grep "affy-master" /etc/hosts | wc -l`
if [[ $CNT -eq 0 ]]; then
  echo "Please add affy-master, affy-slave1 and affy-slave2 to /etc/hosts"
  exit
fi

vagrant ssh master -c "start-all.sh"

vagrant ssh master -c "zkServer.sh start"

echo "Waiting 30 seconds for Hadoop to get started."
sleep 30

echo "Now connect to the master node to start accumulo:"
echo "  vagrant ssh slave2"
echo "  accumulo_home/bin/accumulo/bin/start-all.sh"

