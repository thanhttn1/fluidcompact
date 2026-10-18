# FluidCompact

> **An unofficial intermediary compatibility mod that helps Flowing Fluids understand other mods better.**
> Built for [Flowing Fluids](https://github.com/Traben-0/flowing_fluids) by Traben.
> Not affiliated with or endorsed by its author. It contains no Flowing Fluids code: it only calls its public API.
> **Flowing Fluids is a required dependency** and is not bundled. See [CREDITS.md](CREDITS.md).

Flowing Fluids makes fluids finite. FluidCompact helps it **understand the water pumps of other mods**, which
normally create water from nothing, so those machines drain the world instead.

NeoForge 1.21.1 - license: MIT (`LICENSE.txt`).

## What it does

* **Modern Industrialization** Bronze, Steel and Electric Water Pumps stop creating water: every pumped
  millibucket is drained from the water source blocks around the pump.
* **PneumaticCraft: Repressurized** Liquid Hopper and Gas Lift drain the world instead of duplicating water.
* **Mekanism** Electric Pump and **Mekanism Elements** Seawater Pump drain the world too.
* **Fluidlogged** support so fluid enters and flows through pipes and cables instead of breaking or blocking.
* Pipes (MI, Modern Dynamics, XNet, Refined Storage, ...) never touch world fluid themselves, so they become
  finite automatically once the pump is.
* Fluid removal goes through the Flowing Fluids API. Lava, which Flowing Fluids does not flow by default,
  falls back to a small built-in vanilla drain.
* Jade tooltip and JEI info page for the pumps, a "Mod Compact List" config tab (needs YACL) and its own log
  file `logs/fluidcompact.log`.

## Requirements

* NeoForge 21.1.x, Minecraft 1.21.1
* **Flowing Fluids 1.0.6 or newer (required)**
* Optional: Modern Industrialization, Mekanism, Mekanism Elements, PneumaticCraft, Fluidlogged, Jade, JEI, YACL

## Addon API (`me.thanhtnt2.fluidcompact.api`)

* `FluidCompactApi.service().requestIntake(...)` for your own pumps
* `DrainBackendRegistry.register(...)` to plug in another fluid-physics system
* `FluidIntakeEvent.Pre / Post` on `NeoForge.EVENT_BUS` to cancel or adjust intakes
* `CompatModuleRegistry.register(...)` to add an isolated compat module
* Tags `fluidcompact:managed_fluids`, `fluidcompact:finite_pumps`, `fluidcompact:fluid_passable_blocks`

## Building

Copy the jars listed in `libs/README.txt` into `libs/` (compile-only references, never packed into the jar),
then run `gradlew build`. The result is in `build/libs/`.

## Reporting bugs

Report FluidCompact bugs to thanhtnt2 through GitHub issues (<ADD YOUR GITHUB ISSUES LINK HERE>) and Discord
(<ADD YOUR DISCORD INVITE HERE>), **never to the authors of Flowing Fluids or the other mods**.
Please attach `logs/fluidcompact.log`.

## Credits and license

FluidCompact is MIT licensed. Flowing Fluids (Traben, GNU LGPL 3.0) and every other mod it works with are credited
in [CREDITS.md](CREDITS.md).
