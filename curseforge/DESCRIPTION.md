![OrangePeel](https://raw.githubusercontent.com/ItMeCasey/OrangePeel/main/branding/banners/banner.png)

**See what every bite is worth, and which food in your pack is the best meal.**

OrangePeel shows what food does before you eat it. Hold a food and your hunger bar previews the points it restores, with the saturation and health regeneration it brings. Hover any food, anywhere, and the tooltip tells you the one thing other food mods leave out: how it compares with the rest of the food you are carrying.

![The meal comparator](https://raw.githubusercontent.com/ItMeCasey/OrangePeel/main/branding/banners/h_comparator.png)

Hover a food and OrangePeel ranks it against every other food in your inventory.

- **Ranked:** "Better than 4 of your other 6 foods", with a small bar showing how far it sits from your best one.
- **Best choice:** your best food overall is flagged, and so is the best pick for your hunger right now, so you stop wasting a big meal on a nearly full bar.
- **No waste:** if eating would overfill your hunger bar, the wasted points show in orange.
- **Effects:** names and durations of the effects a food gives.

![Hunger bar preview](https://raw.githubusercontent.com/ItMeCasey/OrangePeel/main/branding/banners/h_hud.png)

- Restored hunger, shown on the bar before you eat, and drawn shanks, saturation and a comparison bar in the food tooltip.
- Golden rails for the saturation you have, and a tooltip line for how long a meal keeps you full.
- Health regeneration preview.
- The exhaustion underlay shows how close the next point of saturation or hunger is to dropping, as a veil over the shanks.
- Every overlay can be turned off, moved, or made more transparent in the config.

![Works with your other mods](https://raw.githubusercontent.com/ItMeCasey/OrangePeel/main/branding/banners/h_compat.png)

- Reads food values from the game, so modded foods work without extra support.
- The lines are added through the game's normal item tooltip, which item viewers such as JEI, EMI and REI use. Every graphic has a plain-text version.
- Other mods can add their own tooltip lines through a small API.

The pictures on this page are illustrations drawn for the listing, not game screenshots.

**Tested:** every layer (Forge and NeoForge on 1.21.1, 26.1.2, 26.2 and 26.3) compiles, passes unit tests, loads on a dedicated server and was run in a real game client with the hunger bar overlays (including the exhaustion underlay, singleplayer) and tooltip seen in screenshots. Item viewers, on NeoForge (plus JEI on Forge 1.21.1): JEI (1.21.1, 26.1.2 and 26.2), REI (1.21.1 and 26.1.2) and EMI (1.21.1) draw the graphical tooltip; BEI works from BEI version 2026.11.4 (BEI's own version number, not OrangePeel's) (2026.11.3 and older draw the text only). JEI 26.3, the other Forge viewers and foods from other mods have not been run; REI 26.2 crashes in its own code and REI 26.3 was not run (its Cloth Config 26.3 library exists now, but the 26.3 test window is not stable yet).

![FAQ](https://raw.githubusercontent.com/ItMeCasey/OrangePeel/main/branding/banners/h_faq.png)

**Does it need to be on the server?** No, except for the exhaustion underlay: the game does not send exhaustion to your client, so that one needs OrangePeel on the server too (singleplayer works on its own). Everything else runs on your client.

**Does it change how food works?** No. It only shows information.

**Can I turn parts off?** Yes. Each overlay and tooltip line has its own setting.

**Fabric?** Not at this time.

**Can I use it in my modpack?** Yes. It's MIT licensed.

Made by ItMeCasey Studios. Source and issues on [GitHub](https://github.com/ItMeCasey/OrangePeel).
