To run:


	In cs455/scaling/ directory:

	Run: gradle build
	Clean: gradle clean

	Start server:
		cd to build/classes
 		Run: java cs455.scaling.server.Server <port> <thread pool size> <batch size> <batch time>

	Start messaging node: 
		cd to build/classes
		Run: java cs455.scaling.client.Client <ip/hostname of server> <port of server> <number of messages to send per second>

	Start multiple nodes:
		Modify node-startup.sh by adding the ip and port of server to above command, and possibly the directory though this should work
		Run: ./node-startup.sh


Files: 
	Gradle: Used for build/cleaning the project
		gradlew
		gradlew.bat
		build.gradle
		settings.gradle
	node-startup.sh: Script to start up multiple messaging nodes at once
	machine_list: Used by node-startup.sh, specifies the hostnames of nodes to startup. Default list is 38 nodes
