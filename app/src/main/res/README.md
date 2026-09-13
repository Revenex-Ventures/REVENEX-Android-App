# app/src/main/res/

Android resource files. All non-code assets used by the app live here.

## Directories

| Directory | Contents |
|---|---|
| `drawable/` | Vector drawables and raster images used in the UI (icons, backgrounds, illustrations) |
| `mipmap-hdpi/` | App launcher icon — 72×72 dp |
| `mipmap-mdpi/` | App launcher icon — 48×48 dp |
| `mipmap-xhdpi/` | App launcher icon — 96×96 dp |
| `mipmap-xxhdpi/` | App launcher icon — 144×144 dp |
| `mipmap-xxxhdpi/` | App launcher icon — 192×192 dp |
| `mipmap-anydpi-v26/` | Adaptive launcher icon (foreground + background layers, API 26+) |
| `values/` | XML value files: `strings.xml` (app strings), `colors.xml`, `themes.xml` |
| `xml/` | Miscellaneous XML configs: network security config, backup rules, file provider paths |

## Notes

- Colors and typography used in Compose screens are defined in Kotlin (`ui/theme/`) — not in `values/colors.xml`.
- `values/strings.xml` holds the app name and any strings that need to be localized.
- The `xml/` network security config controls which HTTP hosts are allowed (important for emulator + local backend calls).
