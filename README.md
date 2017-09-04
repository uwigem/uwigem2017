![UW IGEM Logo](logo.png)

Washington iGEM 2017 Project

# Table Of Contents

 - [Setup](#setup)
 - [Using SSH](#using-ssh)
 - [Known Issues](#known-issues)
 - [Code Structure](#code-structure)

# Setup
1. [Download IMG File](https://www.raspberrypi.org/downloads/raspbian/)
2. Unzip downloaded folder
3. Mount onto a >16GB SD card (right click the .img and select mount)
4. Insert SD card into Raspberry Pi
5. Run Raspberry Pi setup (Monitor, Mouse, Keyboard required)
6. [Setup SSH](##SetupSSH) 
7. [Clone the uwigem2017 git repository](##GitRepoSetup)
8. [Setup the software](#SoftwareSetup)
9. Record the Raspberry Pi's IP address (type `hostname -I`)

## Setup SSH

1. Open a terminal window on Raspberry Pi desktop
2. Type `sudo raspi-config`
3. Press the down arrow until your cursor selects "Advanced Options"
4. Press the right arrow to enter the Advanced Options section
5. Press the down arrow until you get to "SSH"
6. If the SSH setting is "Enabled", you're good, if not, press the right arrow key to enable SSH

## Git Repo Setup

1. Open a terminal window on Raspberry Pi desktop
2. Type `cd ~`
3. Type `git clone https://github.com/uwigem/uwigem2017.git`

## Software Setup

1. Open a file explorer window and navigate to the user home directory (`/home/username/` where username is the main user)
2. Navigate into the folder `raspbian_scripts`
3. Click on each of the scripts in the folder one by one

## Installing NetBeans

1. [Download NetBeans IDE for Java SE](https://netbeans.org/downloads/start.html?platform=linux&lang=en&option=javase)
2. [Refer to these instructions](https://netbeans.org/community/releases/36/install.html#unix)

# Using SSH

1. Download [PuTTy](www.putty.org "putty") (Under `Alternative binary files` select `putty.exe`)
2. Open PuTTy
3. Get the hostname and port number for our Raspberry Pis from Jase
4. Type in the hostname and port number
5. When you are prompted with a user, type in the appropriate main user for that pi
6. Enter the password

You can now remotely use the terminal on that Pi

# Known Issues
- Upon attempting to run Pi4J code following error occurs
~~~
Unable to determine hardware version. I see: Hardware   : BCM2835
,
 - expecting BCM2708 or BCM2709.
If this is a genuine Raspberry Pi then please report this 
to projects @drogon.net. If this is not a Raspberry Pi then you
are on your own as wiringPi is designed to support the 
Raspberry Pi ONLY.
~~~
Work Arounds
 - downgrade firmware using shell script under the Raspbian folder of the old repo (uwigem2017-old)
 - change Pi4J source cdoe and recompile (already done by Jase, not sure if the recompiled version has been uploaded to this repo)

# Code Structure

The entire folder `Tstat` is a NetBeans project folder. 

Once you have cloned the repository onto your development computer, open NetBeans, and open Tstat as a new project.

Tip: If you screw up creating a project and you get the wrong folder as a project, **do not** delete it. Instead, right click, `close`

The `tests` folder is only being occasionally updated and mainly just for Fluids to do tests

# TODO: 
All Todo tasks are located at: https://trello.com/b/Mey7qSgL/drylab-turbidostat

All following neede to be added to the README:
- Add instructions to change the username for the main user as well as the system name for the different berries 
- ^Ask Jase if this is necessary, if we were going to create a standard install 

README Example: https://github.com/rg3/youtube-dl
