# Changelog

Versioning: `1.X.0` for a big update (new mod support), `1.0.X` for a small update or fix.

## 1.3.0

* **New: Alex's Caves.** The Drain now works with Flowing Fluids. When a Copper Valve (or any redstone signal) opens it, the Drain
  takes fluid from above and puts exactly the same amount below, level by level, so no water is created or destroyed.
  The Copper Valve itself needs no hook: it only sends the redstone signal.
* New config section `alexscaves_drain` (speed, interval, sideways spread, effects) and a matching group in "Mod Compact List".
* If the Drain has nowhere to put the fluid, it waits instead of deleting it.

## 1.2.0

* New: Create is checked at startup. Flowing Fluids already supports Create by itself, so FluidCompact adds no Create hooks;
  it keeps its pipe features away from Create's blocks and warns when Flowing Fluids' Create settings contradict finite water.
* New: Auto Balanced Compact (enabled by default). While the server lags, FluidCompact stretches the intervals of its own
  optional housekeeping. It follows Flowing Fluids' own tick-time target and never touches the finite-water logic.
* Optimisation: idle Liquid Hoppers allocate nothing per call and no longer touch the saved data of their chunk.
* Cleanup: one pipe matcher instead of two identical copies.

## 1.1.1

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
