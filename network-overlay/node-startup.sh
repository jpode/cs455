for i in `cat machine_list`
do
echo 'logging into '${i}
dbus-launch gnome-terminal -x bash -c "ssh -t ${i} 'cd /build/classes/java/main; java cs455.overlay.node.MessagingNode saint-paul.cs.colostate.edu 5005;bash;'" &
done
