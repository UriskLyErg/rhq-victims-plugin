rhq-victims-plugin
==================

Victims Plugin to find Jar/War/Sar Files and alert the administrator if they are vulnerable


Install Process

Use maven to package the httptest and server-victims-plugin folders by cd into them then mvn package

Once this is done the plugins should be uploaded to an RHQ server

To do this open your RHQ server and go to the administration tab
Upload both .Jar files for the httptest plugin and server-victims-plugin
Run a "update all plugins" in the bottom left hand corner

Go to inventory
Open the server you wish to apply this plugin to
Update plugins by right-clicking RHQ-Agent plugin and running an Update plugin schedule

Right click on the platform and import rhqvictims

set the port and host to wherever your server is (This can be any agent depending on what you want)
add a list of paths on your computer (To many is super intensive)

Install the server plugin on only one computer or multiple if you want to share the load
This is done by right clicking and importing serverVictim and setting the port to that of the agent plugins

when you want to schedule a scan go to operations schedule new down the bottom 
On the plugin side you can schedule scan which will send victims records across the network
On the server side you can schedule checkCVE which will test all the records


