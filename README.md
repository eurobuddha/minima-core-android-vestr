# Minima Vestr (native Android)

A fully‑native Android port of the **[vestr](https://github.com/minima-global/vestr)** MiniDapp — a
**token‑vesting contract manager** for the [Minima](https://minima.global) blockchain.

Lock tokens in an on‑chain smart contract that releases them gradually over time (linear vesting with a
cliff and a grace period). A **Creator** makes and tracks contracts; a **Collector** withdraws what has
vested.

Unlike the web MiniDapp (React + MDS), this app is native Java and talks to a **Minima Core node running
on the same device** over the node's **broadcast‑Intent IPC** (the `minimaapi` SDK) — no MDS, no RPC, no
browser.

## Interoperable with the web dapp

The app deploys vestr's exact vesting script (`cleanScript`) via `newscript` and adopts the **returned**
address. That address is a deterministic hash of the cleaned script, so it matches the web vestr dapp's —
**contracts created in either app are visible and collectible in the other.**

## How it works (node commands)

| Action | Command(s) |
| --- | --- |
| Deploy script (once) | `newscript script:"<cleanScript>" trackall:false` → returns the contract address |
| List contracts | `coins address:<script> relevant:true` → parse the 11 state vars |
| Create | `send amount:A address:<script> tokenid:T state:{0..8,199}` |
| Collect amount | `runscript` on the contract's `checkMaths` (exact `SIGDIG(2)` value) |
| Collect | `txncreate` → `txninput coinid:.. scriptmmr:true` → `txnoutput` (payout, no‑state) + `txnoutput` (change, keep‑state) → `txnstate port:0..8,199` → `txnpost [burn]` → `txndelete` |

State vars per contract coin: `0` unlock addr · `1` total · `2` start block · `3` end block ·
`4` grace blocks · `5` created ms · `6` start ms · `7` grace hours · `8` end ms · `199` uid.

## Build

Requires a **JDK 17/21** (the Android Studio JBR works; the system JDK may be too new for Gradle):

```sh
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
```

Install the APK, then enable **Minima Vestr** in Minima Core → Apps to authorize the IPC.

## Releases

Versioned APKs + changelog: **[eurobuddha/minima-core-apks](https://github.com/eurobuddha/minima-core-apks)**
(releases tagged `minima-vestr-v<version>`).

## Project layout

- `app/src/main/java/org/minimarex/vestr/` — `MainActivity` (3‑tab shell + pairing + script deploy),
  `Creator`/`Collector`/`About` views, `CreateContractActivity`, `ContractDetailActivity` (collect),
  `CalculatorActivity`, `VestingContract` (script + math), `Contract` (state parser), `NodeApi` (IPC),
  `VestrDesign` (tokens).
- `app/libs/minimaapi.aar` — the Minima Core IPC client SDK.
