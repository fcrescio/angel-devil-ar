# Debug Install

Debug builds use the versioned keystore at `app/keystores/debug.keystore`.

This key is intentionally not secret. It exists only to make CI artifacts installable over previous debug builds with:

```sh
adb install -r app-debug.apk
```

Release builds must never use this keystore.

## 16 KB Page Size Check

The Android CI runs `scripts/apk-alignment-check.sh` after `assembleDebug`. The script uses `zipalign -c -P 16` when `zipalign` is available and keeps the debug build non-blocking while native dependency compatibility is still being validated.
