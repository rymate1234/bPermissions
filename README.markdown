bPermissions 2.x - now under new management
===========================================
![bPermissions header thing](http://dev.bukkit.org/media/images/35/193/simple.png "It looked boring with just text")
Welcome to bPermissions, a Superperms Manager for Bukkit! bPermissions focuses on being unique from the rest of the Permission managers. It's very simple to set up and use, it's lightning fast, fully featured, and has a great support team!

Since codename_B is now focusing on the new 3.x branch of bPermissions, he has handed over control over 2.x to me (rymate1234).

Some features:
--------------
 - bPermissions has a gui client specifically designed to make running a server so easy that you'll forget that you ever used any other permissions plugin. [Get it here! ](http://dev.bukkit.org/server-mods/bpermissionswebgui/)
 - bPermissions has a unique command structure, never before seen in a permission manager. It's best used for adding lots of nodes to a user or group from the console or in-game, less typing!
 - bPermissions has everything you would expect from a Permissions manager. Prefix and suffix support, inheritance, per-player permissions, multiworld support, promotion/demotion tracks, and even group priorities for multi-group setups.
 - Note - bPermissions only defines prefixes and suffixes. You need a chat plugin such as [bChatManager](http://dev.bukkit.org/bukkit-mods/bchatmanager/) for them to show up in chat.

Commands
--------
Say you have a user called 'Bob' and you want to put him in the group 'admin' and give him the prefix 'awesome'. 

 - /world yourmainworld -- selects the world "yourmainworld"
 - /user bob -- selects the user "bob"
 - /user setgroup admin -- set bob's group as "admin"
 - /user meta prefix awesome -- set bob's prefix to "awesome"

You can do the same with groups!

 - /world yourmainworld -- again, selects the world
 - /group admin -- selects the group "admin"
 - /group addgroup moderator -- makes the admin group inherit from the moderator group
 - /group meta prefix &c[Admin] -- sets the admin prefix to "&c[Admin]"

Some other commands are as follows

 - /bpermissions (or /bperms,/bp,/p) -- general commands for bPermissions are prefixed by this
 - /bpermissions import pex -- import a pex permissions.yml in the pex folder
 - /bpermissions reload -- reloads your bPermissions files
 - /bpermissions save -- if auto-save is false, this saves your perms
 - /bpermissions backup -- backup your permissions

In order to use these commands, you'll need the permissions node `bPermissions.admin`
A detailed commands walkthrough is avaliable [on the commands page](http://dev.bukkit.org/bukkit-mods/bpermissions/pages/bpermissions-command-list/)

Example files
-------------
Do "/permissions helpme" to have the plugin print out some example files, or view commented examples below!

 - [Example groups.yml](http://dev.bukkit.org/paste/4596)
 - [Example users.yml](http://dev.bukkit.org/paste/4544/)
 - [Example tracks.yml](http://dev.bukkit.org/paste/4546/)
 - [Example custom_nodes.yml](http://pastie.org/4145721)
 - [Explained config.yml](http://dev.bukkit.org/server-mods/bpermissions/pages/config-yml-explained/)

Promotion tracks
-----------------
1. Define tracks in tracks.yml (use the example file as a guide!)

2. Give people the permission tracks.trackname -- it lets anyone with that node promote people along that track, they don't need bPermissions.admin

3. Want someone to be able to use all tracks? Give them tracks.* --  it lets someone with that node promote anyone along any track name

How do I get support?
---------------------
First, we __highly recommend__ you go read the [FAQ page](http://dev.bukkit.org/server-mods/bpermissions/pages/bpermissions-faq/). If the answer isn't there, there's a few options of support.

 - Visit the [#bananacode irc channel on irc.esper.net](http://webchat.esper.net/?nick=&channels=bananacode). Just head there, as your question, and wait for someone to help you. It might be a while before you get an answer, so be patient!
 - You can [file a ticket](http://dev.bukkit.org/bukkit-mods/bpermissions/tickets/). This is recommended if you don't have the time to hang around in the IRC or you have a bug report. Be prepared to check back to the ticket though, and make sure you attach your bPermissions groups and users yml files to the ticket.
 - Comment on the bPermissions main page.
 - [Watch these videos!](http://www.youtube.com/playlist?list=PL1cGe8gZ2i4cFARgSlOibJrriUj0JWcqm)

However you choose to get support, "pls dont use txt spk," and make sure you use actual grammar.

I'm a developer!
----------------------------
If you wish to hook your plugin into bPermissions, please see the [Developer Page](http://dev.bukkit.org/server-mods/bpermissions/pages/bpermissions-api/) for more information!
If you wish to contribute code to bPermissions, [fork us on GitHub!](https://github.com/rymate1234/bPermissions/)

Thanks for choosing bPermissions
================================
