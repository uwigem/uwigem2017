![UW IGEM Logo](https://andrewhu-uw.github.io/logo.png)

Washington iGEM 2017 Project

README Example: https://github.com/rg3/youtube-dl
# Table Of Contents

 - [Setup](#setup)
 - [Using SSH](#UsingSSH)
 - [Known Issues](#KnownIssues)
 - [Code Structure](#CodeStructure)

# Setup
## Raspberry Pi Setup
1. [Download ISO](https://www.raspberrypi.org/downloads/raspbian/)
2. Unzip downloaded folder
3. Mount onto a SD card (at least 16 GB)
4. Insert Sd card into Raspberry Pi
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

## NetBeans

# Using SSH

1. Download [PuTTy](www.putty.org "putty") (Under `Alternative binary files` select `putty.exe`)
2. Open PuTTy

# Know Issues
- Upon attempting to run Pi4J code following error occurs
~~~
Unable to determine hardware version. I see: Hardware   : BCM2835
,
 - expecting BCM2708 or BCM2709
~~~
Work Arounds
 - downgrade firmware using shell script under the Raspbian folder of the old repo (uwigem2017-old)
 - change Pi4J source cdoe and recompile (already done by Jase, not sure if the recompiled version has been uploaded to this repo)
# Code Structure

The entire folder `Tstat` is a NetBeans project folder. Once you have cloned the repository onto your development computer, open NetBeans, and open Tstat as a new project.

# TODO: 
All Todo tasks are located at: 
All following neede to be added to the README:
- Add full Pi4J Hardware issue error code
- Define mount in RPi Setup
- Flesh out code structure
- Add instructions to change the username for the main user as well as the system name for the different berries 
- ^Ask Jase if this is necessary, if we were going to create a standard install 
