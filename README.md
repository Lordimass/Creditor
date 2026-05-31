# Creditor
A Hytale Plugin and Java Library to credit mod creators and provide information on the mods which are installed.

## Rich credit metadata
Mods can include optional credit metadata in their asset pack under `Server/Credits`.
The asset overrides manifest display fields on the `/credits` page while the manifest remains the fallback.

Example: `Server/Credits/my_mod.json`

```json
{
  "Plugin": "com.example:MyMod",
  "Description": {
    "MessageId": "credits.com.example.my_mod.description",
    "RawText": "My Mod adds useful server features and includes custom credit metadata."
  },
  "License": {
    "MessageId": "credits.com.example.my_mod.license",
    "RawText": "My Mod is licensed under MIT."
  }
}
```