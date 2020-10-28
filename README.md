# Minecraft Server Manager - mcsm
A utility program for owners of minecraft servers that makes it easy to maintain high-quality servers both from the host system and remotely.
The program works by wrapping the standard minecraft server process and reading/writing to it's stdout/stdin streams. 
Currently still very much WIP, but the final feature set will include:

- **Task-Based Server Management** *(Mostly Implemented)*
- **In-Game Chat Connectivity** *(Mostly Implemented)*
- **User Permissions** *(Mostly Implemented)*
- **Flexible Task Scheduling** *(Partially Implemented)*
- **Remote MCSM Connection** *(Barely Implemented)*
- **World Management** *(Unimplemented)*
- **Datapack Management** *(Unimplemented)*
- **Easy Server Backups** *(Unimplemented)*
- **Plugins System** *(Unimplemented)*
- **Modded Server Support** *(Unimplemented/Untested!)*

Since MCSM doesn't modify the minecraft server jar or game files in any way, it isn't a mod and can run on ANY vanilla minecraft server without modification.
It also doesn't create any dependencies for the minecraft server, so it can be removed at any time or the server can be ran through standard means without any adverse effects.