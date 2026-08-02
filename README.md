# chunk-trimming

Cuts chunk writes on Paper servers by saving only chunks a player has visited.

## Terminology

Only chunks that were freshly generated and have not been written to disk yet are considered, so anything a world
already holds stays untouched.

Such a chunk will only be saved on unload if a player was within the `save-radius`, or if something they set in motion
reached into it, e.g. pistons, dispensers, falling blocks, explosions, projectiles or spawned entities. Everything else
is skipped and regenerates from the seed on its next load.

You can use `/chunktrimming` to show skipped and kept chunks.

## Setup

- Paper (or Folia) 1.21+.
- Java 21+.

On startup, the plugin will create a configuration file in `plugins/chunk-trimming/config.yml` where the radius can be
set.

## Links

- Modrinth: https://modrinth.com/plugin/chunk-trimming
- Hangar: https://hangar.papermc.io/EnZaXD/chunk-trimming
- Dev builds: https://build.florianreuth.de/job/chunk-trimming

## Contact

- Issues: https://github.com/florianreuth/chunk-trimming/issues
- Discord: https://florianreuth.de/discord
