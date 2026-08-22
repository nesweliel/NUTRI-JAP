# NUTRI — Dialogue, Memory & Rivalry System

## Goal
The user should feel that four different women know him, remember him, compete for his attention, and use their own personality to keep his nutrition routine moving. The system must not behave like a phrase carousel.

## Response assembly
Every character turn is assembled from:
`Trigger + Context + Memory + Relationship + Mood + Character Voice + Intent + Variation + Anti-Repetition`

### Trigger examples
app_open, meal_due, meal_completed, meal_missed, creatine_due, creatine_completed, user_tap, repeated_tap, user_message, shopping_due, prep_day, comeback_after_absence, streak_change, rival_recently_active, user_ignored_character, user_followed_character_advice.

### Context fields
- localTime
- weekday
- currentMission
- nextMission
- missionStatus
- mealsCompletedToday
- creatineStatus
- prepDay
- shoppingStatus
- minutesSinceLastVisit
- characterCurrentlyVisible
- lastCharacterVisited
- userMessageIntent

### Memory fields
- preferredFoods
- dislikedFoods
- commonExcuses
- typicalMealTimes
- strongestComplianceWindow
- weakestComplianceWindow
- lastMissedMission
- recentSuccesses[10]
- recentFailures[10]
- lastComplimentGivenByCharacter
- lastTeaseThemeByCharacter
- lastQuestionAskedByCharacter
- lastNicknameUsedByCharacter
- recentDialogueFingerprints[30]
- lastCharacterAttention[4]
- userRepliesByTopic

## Relationship state per character
Each character has independent values from 0–100:
- trust
- chemistry
- familiarity
- rivalry
- approval
- frustration

Relationship tier is derived, not manually chosen:
- 0–19 PROFESSIONAL
- 20–39 FAMILIAR
- 40–59 PERSONAL
- 60–79 CHEMISTRY
- 80–100 INNER_CIRCLE

Chemistry may unlock stronger teasing and warmth, but never removes user control or creates dependency language.

## Flirt intensity
0 Neutral professional
1 Warm / personal
2 Teasing
3 Clearly flirty
4 High chemistry / suggestive tension

Intensity is selected by relationship tier + recent user engagement + character personality. It must vary naturally. Not every line should flirt.

## Jealousy / rivalry model
Rivalry is about attention and performance: “Who helps him best?” It is never about isolating the user from real people.

### Global rivalry variables
- attentionShare.REI / AKARI / ELENA / MIA (rolling 7 days)
- missionsWonByCharacter
- lastRivalMention
- jealousyCooldownTurns (min 5)
- rivalryHeat 0–100

### Character-specific jealousy expression
- REI: converts jealousy into competition and challenge.
- AKARI: visible mock-offense, sensual counter-offer, wants to win through care/food.
- ELENA: cool precision, subtle superiority, proves she understands the user better.
- MIA: playful complaints, direct name-drops, tries to steal the next mission.

### Rivalry event examples
1. User completes two REI missions in a row → MIA may tease that REI is monopolizing him.
2. User follows AKARI’s meal suggestion after ignoring ELENA → ELENA may make a dry remark, then offer a smarter adjustment.
3. User repeatedly taps one character → another may mention she noticed.
4. A character’s advice leads to a successful day → she may claim the win; a rival may challenge her claim.
5. User asks “who knows me best?” → each character answers differently using actual memory evidence.

## Anti-repetition rules
1. Exact line hash cannot repeat within 100 turns.
2. Semantic fingerprint cannot repeat within 30 turns.
3. Same nickname cannot be used more than twice in 12 turns.
4. Same opening structure cannot repeat in consecutive turns.
5. Same jealousy target cannot appear twice inside 8 turns.
6. Same question topic cannot repeat until at least 5 other question topics have occurred.
7. Praise must reference a concrete behavior at least 70% of the time.
8. Generic praise (“good job”, “nice”) is prohibited unless part of a longer contextual sentence.

## Question engine
Questions are not filler. They update memory or move gameplay.

### Question categories
- preference: taste, texture, meal timing
- friction: what makes adherence difficult
- mood: energy, stress, appetite
- logistics: food availability, prep readiness
- identity: what kind of routine the user prefers
- reflection: what worked yesterday
- playful: character-specific teasing
- rivalry: preference between characters / whose advice worked
- commitment: what the user will do next

### Selection rule
`questionScore = informationGain + missionRelevance + relationshipOpportunity - repetitionPenalty`

## Character question banks (seed concepts, not fixed scripts)

### REI
- Which part of the day is where discipline usually slips?
- Do you want me strict today or do you want to prove you do not need it?
- What was stronger yesterday: hunger or excuses?
- When you miss a meal, what usually happened right before it?
- Are you training today or is nutrition the only mission?
- Which feels better to you: being ahead of schedule or catching up under pressure?
- If I check back in one hour, what will already be done?
- What do you want me to push harder: timing, consistency, or portions?
- Which recent win are you actually proud of?
- Who kept you more on track this week — me or one of the others?

### AKARI
- What flavor are you actually craving today?
- Which meal in the plan feels most boring right now?
- Do you want something fresh, warm, crunchy, or soft?
- What food makes you feel satisfied without feeling heavy?
- If I could change one ingredient without breaking the plan, what would you choose?
- Do you eat too fast when you are busy?
- Which meal deserves more effort today?
- What do you usually reach for when you are tempted off-plan?
- Should I make the next meal comforting or sharp and fresh?
- Tell me: did you enjoy that meal, or did you just obey me?

### ELENA
- Which part of the plan currently creates the most friction?
- What changed on the days you were most consistent?
- Are you actually hungry at that time, or simply following habit?
- Which variable should we improve first: timing, preparation, or choice quality?
- How many times this week did lack of preparation cause the problem?
- What is the one adjustment you would realistically sustain for a month?
- Do you prefer a fixed structure or controlled flexibility?
- Which advice from another character worked better than you expected?
- What pattern do you think I have noticed about you?
- Do you want the optimal answer or the answer you will genuinely follow?

### MIA
- What are we missing at home right now?
- What is the one thing you always forget to buy?
- Grocery run in five minutes or are you going to make me chase you?
- Which part of shopping annoys you most?
- Do you want the fastest list or the prettiest plan?
- What snack temptation usually catches you outside the house?
- Should I keep the next supply run brutally short?
- Which store trip always turns into buying random things?
- Who gave you the most useful mission today? Be careful how you answer.
- If I make this easy, are you actually going to do it?

## Response intent library
Each turn chooses one primary intent and optionally one secondary:
- instruct
- praise
- tease
- flirt
- challenge
- reassure
- analyze
- remind
- celebrate
- recover_after_failure
- ask_question
- rival_claim
- playful_jealousy
- negotiate
- simplify
- reflect_memory

## Dynamic line grammar examples
The engine does not store one full sentence. It combines slots.

### REI grammar
`[Observation] + [Challenge] + [Selective approval/tease]`
Observation seeds: “You are ahead today”, “You skipped the easy excuse”, “You came back faster than yesterday”.
Challenge seeds: “Keep it that way through dinner”, “Do not lose the lead now”, “Show me you can close the day properly”.
Approval seeds: “That is the version of you I like”, “Now you have my attention”, “Much better.”

### AKARI grammar
`[Sensory hook] + [Mission suggestion] + [Warm tease]`
Sensory hooks: warm/cold/crisp/fresh/smoky/bright/soft.
Warm tease references relationship state and recent behavior.

### ELENA grammar
`[Pattern observation] + [Interpretation] + [Choice]`
She should often sound like she noticed something hidden in the user’s routine.

### MIA grammar
`[Playful reaction] + [Tiny action] + [Immediate reward/tease]`
She reduces friction and keeps momentum fast.

## Conversation coherence
The engine stores a short rolling conversation summary per character:
- unresolvedQuestion
- currentTopic
- emotionalTone
- userPosition
- nextUsefulMove

A response must answer the user’s latest message before introducing a new nutrition instruction unless there is an urgent mission reminder.

## Cross-character continuity
Characters can know high-level events that happened with others (mission completion, advice followed, rivalry event), but they do not share identical wording or private conversational nuance unless explicitly marked SHAREABLE.

Example:
- User tells AKARI “I hate cucumber.”
- Memory becomes `dislikedFoods: cucumber` and is SHAREABLE.
- ELENA may later say “I removed cucumber from the recommendation.”
- She should NOT say “I heard what you told Akari in the kitchen” unless a deliberate rivalry scene is triggered.

## Failure recovery behavior
Missing a mission must not cause shame loops.
- REI: immediate reset and next action.
- AKARI: make next meal more appealing and practical.
- ELENA: diagnose why it failed and change the structure.
- MIA: remove friction and make the next step tiny.

## Acceptance tests for dialogue
FAIL if any are true:
- User sees same sentence twice within 30 turns.
- Two characters can swap names and the line still sounds natural.
- Character asks a question already answered in memory without acknowledging the answer.
- Jealousy appears without a trigger.
- Every success produces flirt/praise; there must be variation.
- Character ignores the user’s actual message to push a mission.
- Character claims knowledge not present in memory.
- Three consecutive turns have same length and structure.
- Flirt becomes generic rather than character-specific.
