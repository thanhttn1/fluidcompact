# Changelog

Versioning: `1.X.0` for a big update (new mod support), `1.0.X` for a small update or fix.

## 1.1.1 - latest

* Liquid Hopper can now also drain the thin leftover water of a pond (partial levels that PneumaticCraft cannot pick up).
* New on/off button in the Liquid Hopper screen, per hopper.
* New options: drain leftover water without a Dispenser Upgrade (off by default), require a connected container, retry delay.
* Works with the tanks of any mod through the standard fluid capability; redstone control is PneumaticCraft's own.

## 1.1.0

* **New: PneumaticCraft: Repressurized.** Liquid Hopper and Gas Lift no longer duplicate water: PneumaticCraft's
  "leave the source block when it has two water neighbours" rule is switched off while water is finite.
  Drones (Liquid Import) already remove every source block, so they need no hook.
* New config section `pneumaticcraft_pickup`, new group in the "Mod Compact List" tab, JEI info page for the pumps.
* Optimisation: the Modern Industrialization hook allocates no block positions while scanning around a pump.
* Optimisation: the pipe cleanup uses a primitive set instead of boxed numbers.
* Checked and documented: the pump statistics are four flat numbers (no block coordinates), and the
  Modern Industrialization code is only reachable behind a mod-presence guard.
