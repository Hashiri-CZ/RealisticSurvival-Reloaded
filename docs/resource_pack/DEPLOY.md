# Deploying a new Harshlands resource pack

Harshlands ships the resource pack via the Bukkit player-pack mechanism. The
URL in `Server/plugins/Harshlands/config.yml` (`ResourcePack.Url`) is sent
to every client on join, and **the client caches the downloaded zip by URL
forever**. There is no SHA-1 hash on the wire (we pass `null` to
`player.addResourcePack`).

The consequence: a client that has cached a stale pack at a given URL will
serve that stale pack on every subsequent join until the URL changes. Any
edit to the zip at the same URL is invisible to existing clients.

## When you edit anything in the pack

1. Regenerate the deployed zip:

   ```bash
   cd "C:/Users/JanOsicka/Desktop/2026BeeHardV2/TODO/Server/plugins/ItemsAdder/contents/Harshlands"
   # zip excludes the dot-files
   jar -cf Harshlands_RP_1.3.2_beta.zip assets overlay_84 pack.mcmeta pack.png
   ```

2. **Cycle the Dropbox URL**:
   - Open Dropbox, find `Harshlands_RP_1.3.2_beta.zip`.
   - Delete the existing shared link (Share → Settings → Remove link).
   - Re-upload the new zip and create a fresh share link. Dropbox issues a
     new `scl/fi/<id>/` URL even if the filename is identical.
   - Make sure the URL ends with `&dl=1` (direct download).

3. Update `ResourcePack.Url` in `Server/plugins/Harshlands/config.yml`.

4. Reload or restart the server.

5. Confirm a freshly-joined client sees the "Downloading resource pack…"
   toast. If it doesn't appear, the URL did not change for that client —
   double-check step 2.

## Why not just send a SHA-1?

We could pass the pack's SHA-1 to `player.addResourcePack` and Mojang's
client would invalidate its cache automatically on content change. We
chose not to for operational simplicity (no second config field to keep in
sync, no "pack failed to load" toast when an operator forgets to update
the SHA but does update the URL). The URL-cycle ritual is the trade-off.
