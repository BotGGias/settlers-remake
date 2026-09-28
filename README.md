# Disclaimer
This fork exists for fun only. 
The Code in this fork is heavily vibe coded. In case you cant deal with that look another way please.



# JSettlers

This project intends to create a remake of the famous strategy game "The Settlers 3" published by Blue Byte in 1998. The project is developed in Java and runs on PC (Windows/Linux) and Android.
MacOS support is broken regardless of CPU architecture.

### Warning: Alpha Status
The game is currently in an **alpha** status! Therefore bugs, frequent changes making saved games invalid and server abortions need to be expected. Nevertheless we will try to minimize trouble.

### Found a Bug? Report it!
If you experience troubles / find a bug ... welp good luck.
Submit your fix if you want or have one, In case i think its legit and i understand what your goal ist i might merge it.
Im not your personal tech support :)
 
## Playing JSettlers

In order to play the game, you need to have the folders "GFX", "SND" and "MAP" (optional) of the original version of "The Settlers 3" as obtained by installing the original "The Settlers 3" game (DEMO version also works).

Furthermore, you need an up-to-date installation of [Java 11 or newer](https://adoptium.net/). A Java Installation is needed to run the desktop version of JSettlers, as it is written in the programming language Java.

Java is preinstalled on Android.

After that, follow the detailed installation instructions for you platform.

### Windows and Linux
1. Install "The Settlers III" or a demo version ([Settlers III Amazons Demo](http://www.siedler-maps.de/downloads.php?action=download&downloadid=41)) of it. Don't worry, if it is not running on your OS, we only need the graphics and sound files. In order to get them, you can also unzip the Amazons Demo exe file (yes: unzip the .exe) and copy the folders `Gfx`, `Snd` and `Map` (optional) into an empty folder on your computer.
2. Download the newest stable [release of JSettlers*.zip](https://github.com/paulwedeck/settlers-remake/releases) (this also includes the MapEditor).
3. Unpack the downloaded archive to wherever you want JSettlers' installation to be.
5. Run the "JSettlers.jar" file.
  1. On the first start, the game will ask you for the folder where you've installed / unziped (see step 1) the original Settlers III. Please select the respective folder and continue.
  2. Have fun and enjoy the game!
6. Please have a look at the [manual](https://github.com/jsettlers/settlers-remake/wiki/JSettlers-Manual). The current state of the game lacks some controls known from the original, but also contains new ways to do things, which you shouldn't miss. 

#### Arch Linux
1. Install [jsettlers-git](https://aur.archlinux.org/packages/jsettlers-git/) from the AUR.
2. Optionally: Install [settlers3-demo-data](https://aur.archlinux.org/packages/settlers3-demo-data/) if you don't own an original The Settlers III and select the following folder when the game asks you: /usr/share/jsettlers/s3
3. You can start the game from the system-menu or with the commands "jsettlers" and "jsettlers-mapcreator".
4. See instructions above

#### Configuration Flags
As described before, the game's UI is still lacking a lot of features. That's why we have to offer some configurations via an options file. You can find a default `options.prp` file aside the `JSettlers.jar` file after you unpacked the archive. 

When opening the file, you will see several options that can be enabled by uncommenting them (remove the # at the beginning of the respective line). This is also described in the file. 

**Possible configurations include:**
- all-ai: Let all players be played by the AI. You will be able to watch all AI players and to "assist" them during the game.
- fixed-ai-type=YYYYY: Option to specify an AI type that shall be used for all AI players. The default behavior is to use a the weakest AI type for the first player and increase the difficulty for every player. Possible values: ROMAN_VERY_EASY, ROMAN_EASY, ROMAN_HARD, ROMAN_VERY_HARD
- disable-ai: If this flag is enabled, no AI players will be present in single player games. 
- locale: If you want to test a different localization than your systems default, it can be specify with this option. The value should look like: en_en.

**Command line flags**
All the options above can also be specified as command line options. For this, you need to prepend every one of them with a double dash. Therefore the option `fixed-ai-type=YYYYY` should be specified as `--fixed-ai-type=YYYYY`.


### Android
1. Enable installation of Apps from "Unknown Sources".
2. Copy the folders "GFX", "SND" and "MAP" (optional) of your original "The Settlers 3" installation into a folder called "JSettlers" on your device. The "JSettlers" folder must be located in the root directory of your internal storage (alongside folders like "Download" or "DCIM") or your external storage (e.g. memory card).
3. Download the newest stable JSettlers-Android_v*.apk onto your Android device.
4. Install JSettlers by running the downloaded file.

## Build instructions and developer's guide
The [build instructions](https://github.com/paulwedeck/settlers-remake/wiki/Compiling-using-gradle) and the [developer's guide](https://github.com/paulwedeck/settlers-remake/wiki/Developer's%20Guide) can be found in our wiki.

Building JSettlers requires a JDK 17 or newer (JDK 21 is recommended), because the build uses Gradle 8 and the Android Gradle Plugin 8. The built game still runs with Java 11 or newer.

### Textures
JSettlers is meant to be used with the original textures from Settlers 3.
However we are working on replacing these with new textures that have a more permissive license.
There is a [list](https://github.com/paulwedeck/settlers-remake/blob/master/TEXTURES.md) of textures that we didn't create ourselves but instead took from image sites on the internet



## Getting in Touch
Besides the possibility to report bugs on Github you can also join our [JSettlers Discord](https://discord.gg/2hVV4u6). Here you can discuss on development questions and find other players to meet with. 
