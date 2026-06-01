# Creditor
Creditor was designed and created by Hytale Workshop following a conversation regarding the problem of server owners 
unintentionally (or in rare cases, intentionally) not crediting the creators of the mods which they use on their 
server. Creditor adds a new `/credits` command which shows a list of installed mods and their creators, as well as 
other metadata from the manifest.

The mod can be included as a Java library in your own mod (process described below) or it can be installed independently
by a good faith server owner. When installed independently, the credits menu adds a checkmark badge in the upper right 
which tells players that the server owner is actively choosing to support modders. If the symbol is missing, the mod 
was installed transitively as a Java library in some other installed mod, and the server owner has not chosen to credit 
creators. 

**This works best for the community if as many people as possible package this with their mod!**

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

When you build your mod, make sure to use the shadow plugin to ensure the dependency is bundled with your mod
```groovy
plugins {
    idea
    java
    ...
    id("com.gradleup.shadow") version "8.3.6"
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        
        manifest {
            attributes(
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version,
                    "Multi-Release" to "true"
            )
        }
        
        from("src/main/resources/hytale-assets") {
            exclude("manifest.json")
        }

        // Remove the nested hytale-assets/ copy from the JAR (assets are at root now).
        // The nested copy still exists in build/resources/main/ for deploy.sh compatibility.
        exclude("hytale-assets/**")

        // Exclude signature files (cause issues)
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/LICENSE*")
        exclude("META-INF/NOTICE*")
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false // Only use shadowJar
    }
}
```

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