# Creditor
A Hytale Plugin and Java Library to credit mod creators and provide information on the mods which are installed.

## Installation
**Creditor can be installed as a mod on the server by putting the JAR file in the mods folder as normal**, but it can also
be used as a Java Library and packaged in your mod. Add Creditor to your project via Cursemaven:
```groovy
repositories {
    maven { url "https://www.cursemaven.com" }
}

dependencies {
    implementation "curse.maven:dialogue-<project-id>:<file-id>"
}
```
*Note: The `file-id` is the ID of the file on CurseForge, find the file you want and copy the ID from its URL 
`curseforge.com/hytale/mods/creditor/files/FILE_ID_IS_HERE`*

Creditor requires initialisation as a Hytale plugin, so in your `Main.start()` and `Main.setup()` methods, run 
`Creditor.start()` & `Creditor.setup()` respectively.

## Rich credit metadata
Mods can include optional credit metadata in their asset pack under `Server/Credits`.
The asset overrides manifest display fields on the `/credits` page while the manifest remains the fallback.

Example: `Server/Credits/my_mod.json`

```json
{
  "Plugin": "com.example:MyMod",
  "Description": {
    "MessageId": "myMod.myMod.description",
    "RawText": "My Mod adds useful server features and includes custom credit metadata."
  },
  "License": {
    "MessageId": "myMod.myMod.license",
    "RawText": "My Mod is licensed under MIT."
  }
}
```