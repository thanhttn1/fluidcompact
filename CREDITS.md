# Credits and attribution

## Flowing Fluids (required)

**FluidCompact is an unofficial, independent intermediary mod for Flowing Fluids.**
It is not made, endorsed or supported by Flowing Fluids' author.

* Mod: **Flowing Fluids**, by **Traben**
* License: **GNU Lesser General Public License 3.0 (LGPL-3.0)**
* Source: https://github.com/Traben-0/flowing_fluids

What FluidCompact does with it, stated plainly:

* Flowing Fluids is a **required dependency**. FluidCompact does not work without it, the game stops with a
  "missing dependency" message, and the Flowing Fluids jar is **never bundled** inside the FluidCompact jar.
* FluidCompact **does not contain any code, assets or data from Flowing Fluids**. It only calls the public
  Flowing Fluids API (`FlowingFluidsAPI`) at runtime, the same way any addon would. The Flowing Fluids jar is
  used purely as a compile-time reference while building.
* The goal is the opposite of copying: FluidCompact teaches Flowing Fluids' world about other mods' water pumps
  (Modern Industrialization, Mekanism, ...) so those machines drain the world instead of creating fluid.
* All the fluid physics people enjoy are Traben's work. Please support the original mod and report
  any issues regarding the FluidCompact mod to me (via my GitHub page); please do not flood the author page.

## Other mods FluidCompact works with

None of these are bundled or modified. FluidCompact references them by name or through their public API.
Licenses below are as declared in the mods' own metadata (versions we tested against).

| Mod | Author(s) | License |
| --- | --- | --- |
| Modern Industrialization 2.5.x | Azerococo, Technici4n | MIT |
| Mekanism 10.7.x | Aidancbrady and contributors | MIT |
| Mekanism Elements 3.0.x | FixedDolphin927, NadienDev | MIT |
| Fluidlogged 2.0.x | Leximon | MIT |
| Jade 15.x | Snownee | CC-BY-NC-SA-4.0 |
| Just Enough Items 19.x | mezz | MIT |
| YetAnotherConfigLib 3.8.x | isXander | LGPL-3.0-or-later |

Also checked for compatibility (no changes needed): Modern Dynamics (Technici4n, LGPLv3), XNet (McJty, MIT),
Refined Storage (Refined Mods, MIT), Tesseract API (Swedz, MIT), Mekanism Generators (MIT), Mekanism:More Machine (MIT).

## FluidCompact

Copyright (c) 2026 thanhtnt2, released under the MIT License (see `LICENSE.txt`).
Build scripts derive from the NeoForged MDK template (MIT, Copyright (c) 2023 NeoForged project).
