for i in `cat machine_list`
do
echo 'logging into '${i}
dbus-launch gnome-terminal -x bash -c "ssh -t ${i} 'cd cs455/scaling/build/classes/java/main; java cs455.scaling.client.Client raleigh.cs.colostate.edu 5001 2;bash;'" &
done
