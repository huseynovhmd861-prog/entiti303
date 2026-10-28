Entity 303 - Forge 1.12.2 mod (source project)

BUILD (JDK 8 required):
 1. Download the Forge 1.12.2 MDK (forge-1.12.2-14.23.5.2860-mdk) from files.minecraftforge.net
 2. Copy the MDK's gradlew, gradlew.bat and gradle/ folder into this folder
 3. Run:  gradlew setupDecompWorkspace   then   gradlew build
 4. Put build/libs/Entity303-1.0.jar into your .minecraft/mods folder (Forge 1.12.2)

IN GAME:
 Creative inventory -> spawn eggs, or:  /summon entity303:entity_303

BUILD WITHOUT INSTALLING ANYTHING (GitHub):
 1. Create a free GitHub account, make a new repository
 2. Upload the CONTENTS of this folder (build.gradle, src, .github ...) to it
 3. Open the "Actions" tab -> "Build jar" -> wait for green check
 4. Open the finished run -> Artifacts -> download Entity303-jar (zip containing the .jar)
