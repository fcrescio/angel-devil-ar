# Character Joint Map

## Trellis Winged Devil

Runtime asset:

`app/src/main/assets/models/trellis_winged_devil.glb`

The asset has 46 skin joints with generic names (`joint_0` through `joint_45`)
and no authored animation clips. The mapping below is inferred from offline
joint-sweep diagnostics, not from semantic bone names.

Generate the diagnostic images with:

```sh
./scripts/render-skinned-joint-sweep.py app/src/main/assets/models/trellis_winged_devil.glb --output-dir /tmp/angel-asset-renders/joint-sweep
```

Useful generated files:

- `neutral.png`: front, back, top, and bottom neutral silhouette.
- `summary.tsv`: moved vertex count, max/mean delta, influence center, and
  influence extents for every joint.
- `joint_XX_joint_XX.png`: per-joint neutral/deformed overlay.

## Reading The Sweep

The per-joint images use:

- Gray points: neutral skinned vertex cloud.
- Red points: vertices after rotating one joint by an exaggerated angle.
- Views: front, back, top, bottom.

Large global movers are useful for understanding hierarchy but are risky for
expressive runtime animation because they affect too much of the body.

## Preliminary Candidates

| Joint | Current Interpretation | Confidence | Notes |
| --- | --- | --- | --- |
| `joint_23` | broad root/global body | High | Moves all 23,371 vertices; useful only as hierarchy/root evidence. |
| `joint_21` | broad upper/body parent | Medium | Moves most of the model; too global for targeted motion. |
| `joint_17` | broad upper/body parent | Medium | Moves torso/upper mass heavily; likely a parent in the chain. |
| `joint_19` / `joint_20` | upper torso/shoulder mass | Medium | Same influence center near upper body; good candidate for breathing/idle sway experiments. |
| `joint_14` | right upper appendage/wing-side region | Medium | High influence center, right side in front view. |
| `joint_27` / `joint_32` | left upper appendage/wing-side region | Medium | Mirrors the right-side upper region more than most other joints. |
| `joint_16` / `joint_11` / `joint_8` | right arm-side chain | Medium | Strong right-side deformation; device testing showed these read as arm motion, not wing motion. |
| `joint_25` / `joint_31` / `joint_36` | left arm-side chain | Medium | Strong left-side deformation; device testing showed these read as arm motion, not wing motion. |
| `joint_24` / `joint_26` / `joint_29` / `joint_40` | tail or lower/back appendage | Medium | Best candidates for tail motion; top view shows long back/lower extension. |
| `joint_0` through `joint_6` | small right lower chain | Low | Localized and low movement; likely distal limb/foot/detail joints. |
| `joint_38` through `joint_45` | small left lower chain | Low | Localized and low movement; likely distal limb/foot/detail joints. |

## Animation Implications

First runtime bone experiments should avoid `joint_23`, `joint_21`, and
`joint_17`; they are too broad and will make the character collapse or twist as
a whole.

Safer first tests:

- idle torso pulse: `joint_19` or `joint_20` with a very small angle.
- wing twitch: one side from `joint_14`/`joint_9`, mirrored against
  `joint_27`/`joint_32`.
- tail twitch: `joint_24`, `joint_26`, or `joint_29`, with visual verification
  on device.

## Runtime Idle Animation Pass

The current runtime idle pass uses the following conservative subset:

| Runtime Role | Joint |
| --- | --- |
| right wing root | `joint_14` |
| right wing tip | `joint_9` |
| left wing root | `joint_27` |
| left wing tip | `joint_32` |
| tail root | `joint_24` |
| tail mid | `joint_26` |
| tail tip | `joint_29` |

`DevilProceduralMotion` drives an asymmetric idle wing flap, a lagged wing-tip
response, a slower tail curl, and a subtle breathing scale. The wing cycle
intentionally avoids a plain sine wave: opening is slower than closing, the tips
trail the roots, and each cycle has a small deterministic amplitude variation.
It also defines first-pass procedural poses for appearing, searching, and
blocked states using the same validated wing and tail joints.
`DevilRigAnimator` applies those rotations to Filament joint entities at runtime
and calls `Animator.updateBoneMatrices()` afterward.
This is still a provisional Trellis-specific map. The first device test showed
`joint_16`/`joint_11` and `joint_31`/`joint_36` moving arms instead of wings, so
the runtime pass now uses the higher wing-side `joint_14`/`joint_9` and
`joint_27`/`joint_32` chains.

The next refinement should render axis sweeps (`x`, `y`, `z`) for the safest
candidates, because the current diagnostic uses one default axis and a joint can
look good on one axis while being unusable on another.
