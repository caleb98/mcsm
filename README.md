# Minecraft Server Manager - mcsm
A utility program for owners of minecraft servers that makes it easy to maintain high-quality servers both from the host system and remotely.
The program works by wrapping the standard minecraft server process and reading/writing to it's stdout/stdin streams. This allows administrators to issue commands in game that are capable of creating server backups, restarting the server, etc. No more needing to SSH into the host system!  

**Current features include:**

- Task-Based Server Management
- In-Game Chat Connectivity
- User Permissions
- Task Scheduling
- World Management
- Easy Server Backups

**Partially Implemented:**

- Remote MCSM Connection *(Will add more features after core development is further along)*
- Modded Server Support *(Untested)*

**Future Additions**

- Datapack Management
- Plugins System

Since MCSM doesn't modify the minecraft server jar or game files in any way, it isn't a mod and can run on ANY vanilla minecraft server without modification.
It also doesn't create any dependencies for the minecraft server, so it can be removed at any time or the server can be ran through standard means without any adverse effects.
