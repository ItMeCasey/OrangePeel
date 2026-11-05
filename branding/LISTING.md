# OrangePeel listing kit: CurseForge

Everything you need to fill in the CurseForge project form. Files are in `branding/icon/` and `branding/banners/`.

## Project fields

| Field | CurseForge |
|---|---|
| Project ID | (fill in once Casey has created the project) |
| Name | OrangePeel |
| Slug / URL | `orangepeel` |
| Mod id | `orangepeel` |
| Icon / logo | `icon/icon_512.png` (square, at least 400x400) |
| Class | Mods |
| Main category | Map and Information (or the closest Food or Utility & QoL category the form offers) |
| More categories | Food, Utility & QoL, if offered |
| Loaders | Forge and NeoForge |
| Game versions | Minecraft 1.21.1, 26.1.2, 26.2 and 26.3, one jar per Minecraft version; set on each uploaded file |
| Environment | Client (the server does not need it) |
| License | MIT |
| Source | https://github.com/ItMeCasey/OrangePeel |
| Issues | https://github.com/ItMeCasey/OrangePeel/issues |
| Wiki | https://itmecasey.com/wiki/orangepeel/ (not deployed yet; add the link once it is live) |

### One-line description

> See how every food fills you, and which one in your pack is the better meal. A food HUD and tooltip for Forge and NeoForge that compares foods against what you carry.

Use it for the CurseForge summary (it is under 250 characters), social posts and the top of the description.

### Gallery

| File | Use |
|---|---|
| `banners/features.png` (1920x1080) | First gallery image |
| `banners/tooltip.png` (1920x1080) | The meal comparator on three foods, second gallery image |
| `banners/hud.png` (1920x1080) | The hunger bar preview, third gallery image |
| `banners/social.png` (1280x640) | GitHub social preview (repo Settings > Social preview) |
| `banners/banner.png` (800x150, saved at 2x) | Top of the description |
| `banners/h_*.png` (800x46, saved at 2x) | Section headers inside the description (the description uses comparator, hud, compat and faq) |

The panels in these images are illustrations drawn for the branding, not game screenshots. Add real in-game screenshots to the gallery once the mod has been played in a client.

### Description

The CurseForge description is in [`curseforge/DESCRIPTION.md`](../curseforge/DESCRIPTION.md). Paste it with the description type set to **Markdown**. Its images load from GitHub, so `branding/` has to be pushed first. It has no Install section on purpose, and its Tested line stays "to be filled in at release" until someone has played it.

## Checklist

1. Push `branding/` to GitHub so the description images load.
2. Create the CurseForge project with `icon_512.png` as the logo, then record the project id above. New projects are reviewed before they go live.
3. Set `banners/social.png` as the GitHub social preview.
4. Fill in the Tested line in the description at release.
5. Optional: `icon/orangepeel_logo.png` is the jar's mod list logo.
