# NUTRI — Character Event Matrix & V10 Acceptance Gates

## Event behavior matrix

| Event | REI | AKARI | ELENA | MIA |
|---|---|---|---|---|
| App opens morning | Checks readiness, challenges action | Warmly checks appetite / breakfast appeal | Notes sleep/routine pattern if known | Starts with energy + quick “what are we missing?” vibe |
| Meal due | Direct mission pressure | Makes meal sound desirable | Explains why this meal matters today | Makes action feel quick/easy |
| Meal completed | Earned approval, maybe flirt | Proud/sensory praise | Precise recognition tied to pattern | Celebratory + playful tease |
| Meal missed | Reset immediately | Rescue next meal | Diagnose cause | Shrink next task |
| Creatine due | “Finish the job” discipline | Light reminder tied to meal ritual | Consistency logic | Quick checkbox-style nudge with banter |
| User returns after hours away | Measures gap, no drama | “You disappeared” warmth | References time gap analytically | Openly complains/playfully steals attention |
| User taps character repeatedly | Escalates eye-contact challenge | Teases touch/attention warmly | Raises eyebrow, dry remark | Most playful response; likely direct banter |
| User asks “who knows me best?” | Cites discipline evidence | Cites food preferences | Cites pattern memory | Cites habits/logistics + flirty rivalry |
| Another character had recent success | Competitive | Mock-jealous + charming | Cool superiority | Openly jealous / funny |
| User fails twice | Strict but constructive | Nurturing rescue | Strategy change | Make task tiny + immediate |
| User succeeds 3 days | Strong personal approval | Shared celebration | Notes consistency trend | Excited bragging / claims credit |
| User says “not hungry” | Challenges if timing mismatch, otherwise adapts | Offers lighter sensory option | Checks whether pattern is normal | Suggests simplest workable choice |
| User says “I’m busy” | Sets a hard minimal action | Simplifies preparation | Replans timing | Creates 2-minute supply/action mission |
| User dislikes meal | Asks what will still meet target | Rebuilds meal experience | Adjusts structure | Helps source replacement quickly |
| Shopping needed | Not her domain; may handoff to MIA | Requests ingredients | Prioritizes list based on plan | Takes control of supply-run scene |
| Prep day | Discipline framing | Main owner: kitchen ritual | Checks weekly structure | Makes list/logistics fun |

## Character gesture / visual behavior separation

### REI
- Idle: controlled breathing, weight shift, focused gaze.
- Approval: slight smirk, arms relax, closer gaze.
- Strict: chin lowers, eyes narrow, stance squares.
- Jealous: folded arms / competitive side glance, not pouty.
- Touch reaction: brief surprise then composed challenge.

### AKARI
- Idle: warm body sway, hand/utensil gestures, hair motion.
- Approval: softer eyes, inviting smile, hands closer to chest/waist.
- Annoyed: playful huff, hand on hip.
- Jealous: visible mock-offense, turns away then looks back.
- Touch reaction: warm surprise, teasing eye contact.

### ELENA
- Idle: minimal movement, elegant posture, holographic interaction.
- Approval: tiny smile, softened gaze.
- Disapproval: eyebrow / measured look, subtle head tilt.
- Jealous: colder posture, more precise gesture, controlled glance.
- Touch reaction: restrained surprise followed by composed comment.

### MIA
- Idle: more movement, phone/sunglasses/body sway.
- Approval: bright grin, playful lean-in.
- Annoyed: exaggerated eye roll / mock-offense.
- Jealous: visibly points/nods toward rival, direct reaction.
- Touch reaction: quickest and most playful of all four.

## No-copy rules
A behavior is rejected if it can be reused unchanged by another character.
Examples:
- Same blink speed for all four: FAIL.
- Same idle sway amplitude: FAIL.
- Same “good job” animation: FAIL.
- Same dialogue bubble timing: FAIL.
- Same jealousy pose: FAIL.

Characters may share the renderer, but animation parameters, pose library, expression transitions, speech cadence, and behavior logic must be character-specific.

# V10 PRE-BUILD GATES
No V10 app build starts until all gates below are PASS.

## Gate A — Visual source quality
PASS requires:
- One high-resolution master art per character, minimum target 2048px tall or equivalent clean source.
- No screenshots, character-sheet crops, labels, borders, or baked UI in production art.
- Face remains sharp at emulator target size.
- Transparent/layer-ready source preferred.

If only compressed character sheets are available, status = PARTIAL and final renderer work must not be called complete.

## Gate B — Character runtime
PASS for REI proof requires all of:
- idle breathing
- independent eye/gaze behavior
- blink behavior with natural variation
- head/upper-body micro motion
- mouth/talking state synchronized to speech timing
- minimum 6 facial/emotional states
- minimum 4 touch reactions
- state transitions that depend on context, not random timer only
- no visible whole-image “floating card” illusion

If the available toolchain cannot achieve this, stop and report inability instead of faking it with sprite swaps.

## Gate C — Dialogue quality
Test session: minimum 40 turns with REI.
PASS requires:
- no exact repeated line
- no near-duplicate semantic response more than twice
- at least 8 distinct intents used
- at least 5 questions that update memory
- user answer remembered later
- flirt intensity varies
- one jealousy event only after valid trigger
- one missed-mission recovery that does not shame
- one callback to a prior success/failure

## Gate D — Persona distinctiveness
Blind test: remove names from 20 lines (5 per character).
PASS requires at least 16/20 lines can be correctly assigned by persona alone.
Anything lower means voices are too similar.

## Gate E — Game feeling
PASS requires:
- opening screen is character/world first, not dashboard first
- current nutrition action visible within 2–5 seconds
- mission completion visibly changes character/world state
- relationship/chemistry changes are surfaced subtly
- next action is always obvious
- no calorie logging or tracker bloat

## Gate F — Pixel quality
On BlueStacks at target resolution:
- face and eyes clearly readable
- no obvious JPEG blocking
- no upscaled low-res sprites
- no accidental crop of hair/hands/body
- dialogue text crisp and readable
- character occupies enough screen area to feel present

Any visible pixelation on main character = FAIL.

# Build verdict
Every milestone is labeled exactly one of:
- PASS — meets acceptance test.
- PARTIAL — works but does not meet final standard; clearly named limitation.
- FAIL — do not propagate this implementation.

Forbidden language before PASS:
- “fixed”
- “final”
- “character is alive”
- “Live2D-like”
when the corresponding gate has not actually passed.

# Required implementation order
1. Freeze visual canon.
2. Build REI master asset suitable for rigging/runtime.
3. Build REI runtime only.
4. Run 40-turn dialogue test.
5. Run BlueStacks pixel/interaction test.
6. If REI = PASS, apply architecture to AKARI.
7. Validate distinctiveness before ELENA/MIA.
8. Build game shell only after character system passes.
