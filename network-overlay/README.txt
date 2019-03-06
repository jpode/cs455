To run:


	In /network-overlay/ directory:

	Run: gradle build

	Start registry - Run: java cs455.overlay.node.Registry <port>

	Start messaging node - Run: java cs455.overlay.node.MessagingNode <ip/hostname of registry> <port of registry>
	Start multiple nodes:
		Modify node-startup.sh by adding the ip and port of registry to above command
		Run: ./node-startup.sh


Files: 
	Gradle: Used for build/cleaning the project
		gradlew
		gradlew.bat
		build.gradle
		settings.gradle
	node-startup.sh: Script to start up multiple messaging nodes at once
	machine_list: Used by node-startup.sh, specifies the hostnames of nodes to startup. Default list is 10 nodes