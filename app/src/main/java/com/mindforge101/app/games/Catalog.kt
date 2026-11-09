package com.mindforge101.app.games

import com.mindforge101.app.domain.Category
import com.mindforge101.app.domain.Category.ATTENTION
import com.mindforge101.app.domain.Category.EXECUTIVE
import com.mindforge101.app.domain.Category.LANGUAGE
import com.mindforge101.app.domain.Category.LOGIC
import com.mindforge101.app.domain.Category.MATH
import com.mindforge101.app.domain.Category.MEMORY
import com.mindforge101.app.domain.Category.PROBLEM
import com.mindforge101.app.domain.Category.SPATIAL
import com.mindforge101.app.domain.Category.SPEED
import com.mindforge101.app.domain.Category.VISUAL
import com.mindforge101.app.domain.GameDef
import com.mindforge101.app.domain.GameTemplate

object Catalog {
    private fun g(id: String, name: String, cat: Category, blurb: String, t: GameTemplate) =
        GameDef(id, name, cat, blurb, t)

    val games: List<GameDef> = listOf(
        // Memory
        g("memory-matrix", "Memory Matrix", MEMORY, "Recall which squares were marked", GridMemory(3)),
        g("grid-recall", "Grid Recall", MEMORY, "A bigger grid to remember", GridMemory(4)),
        g("sequence-recall", "Sequence Recall", MEMORY, "Repeat the flashing order", SequenceMemory(4)),
        g("pattern-pads", "Pattern Pads", MEMORY, "Longer order with more pads", SequenceMemory(6)),
        g("number-memory", "Number Memory", MEMORY, "Hold a growing number in mind", NumberMemory()),
        g("word-recall", "Word Recall", MEMORY, "Which word was on the list?", RecallChoice(Words.byLength.getValue(5), "\n")),
        g("symbol-recall", "Symbol Recall", MEMORY, "Which symbol did you see?", RecallChoice(listOf("●", "■", "▲", "◆", "★", "✚", "♠", "♣", "♥", "♦", "☀", "☾"))),
        g("letter-recall", "Letter Recall", MEMORY, "Which letter did you see?", RecallChoice(('A'..'Z').map { it.toString() })),

        // Attention
        g("odd-shape", "Odd Shape", ATTENTION, "Find the shape that differs", OddGlyph(listOf("●" to "■", "■" to "◆", "▲" to "△", "●" to "○", "◆" to "◇"))),
        g("odd-letter", "Odd Letter", ATTENTION, "Find the letter that differs", OddGlyph(listOf("A" to "X", "E" to "F", "O" to "Q", "b" to "d", "p" to "q"))),
        g("odd-digit", "Odd Digit", ATTENTION, "Find the digit that differs", OddGlyph(listOf("1" to "8", "3" to "8", "5" to "6", "6" to "9", "6" to "8"))),
        g("odd-arrow", "Odd Arrow", ATTENTION, "Find the arrow that differs", OddGlyph(listOf("↑" to "↓", "→" to "←", "↑" to "→", "↑" to "↗", "↗" to "→"))),
        g("target-count", "Target Count", ATTENTION, "Count the triangles", CountTargets("▲", listOf("●", "■", "◆"))),
        g("letter-count", "Letter Count", ATTENTION, "Count one letter in a crowd", CountLetters()),

        // Logic
        g("number-series", "Number Series", LOGIC, "Spot the rule, find what's next", NumberSeries(listOf(SeriesKind.ARITH, SeriesKind.GEO))),
        g("alternating-series", "Alternating Series", LOGIC, "Two rules, woven together", NumberSeries(listOf(SeriesKind.ALT))),
        g("power-series", "Power Series", LOGIC, "Squares, triangles and sums", NumberSeries(listOf(SeriesKind.SQUARES, SeriesKind.TRIANGULAR, SeriesKind.FIB))),
        g("letter-series", "Letter Series", LOGIC, "Find the next letter", LetterSeries()),
        g("pattern-next", "Pattern Next", LOGIC, "Continue the shape pattern", PatternNext()),
        g("odd-number-out", "Odd Number Out", LOGIC, "One number breaks the rule", OddNumberOut()),
        g("logic-chain", "Logic Chain", LOGIC, "Link the clues to find who", LogicChain()),

        // Math
        g("quick-add", "Quick Add", MATH, "Fast addition", Arithmetic("+")),
        g("quick-subtract", "Quick Subtract", MATH, "Fast subtraction", Arithmetic("-")),
        g("times-tables", "Times Tables", MATH, "Mental multiplication", Arithmetic("*")),
        g("mental-mix", "Mental Mix", MATH, "All three operations", Arithmetic("+-*")),
        g("missing-number", "Missing Number", MATH, "Fill in the equation", MissingOperand()),
        g("missing-operator", "Missing Operator", MATH, "Which sign makes it true?", MissingOperator()),
        g("estimate-it", "Estimate It", MATH, "Round in your head", EstimateIt()),
        g("fraction-match", "Fraction Match", MATH, "Which fraction is larger?", CompareFractions()),
        g("prime-hunter", "Prime Hunter", MATH, "Find the prime number", PrimeHunt()),

        // Language
        g("word-scramble", "Word Scramble", LANGUAGE, "Unscramble the word", WordScramble(4, 8)),
        g("anagram-sprint", "Anagram Sprint", LANGUAGE, "Long words, short clock", WordScramble(6, 8, 0.7f)),
        g("spelling-fix", "Spelling Fix", LANGUAGE, "Pick the correct spelling", SpellingFix()),
        g("synonym-match", "Synonym Match", LANGUAGE, "Same meaning", WordRelation(Relation.SYNONYM)),
        g("antonym-match", "Antonym Match", LANGUAGE, "Opposite meaning", WordRelation(Relation.ANTONYM)),
        g("category-sort", "Category Sort", LANGUAGE, "Which word doesn't fit?", WordRelation(Relation.CATEGORY)),

        // Spatial
        g("rotate-arrow", "Shape Rotation", SPATIAL, "Turn it in your head", RotateArrow()),
        g("mirror-match", "Mirror Match", SPATIAL, "Mirror and flip", MirrorArrow()),
        g("compass-turns", "Compass Challenge", SPATIAL, "Keep your bearings", CompassTurns()),
        g("path-finder", "Path Finder", SPATIAL, "Follow the moves", PathSteps(false)),
        g("distance-home", "Distance Home", SPATIAL, "How far from the start?", PathSteps(true)),

        // Speed
        g("quick-compare", "Quick Compare", SPEED, "Tap the larger number", QuickCompare()),
        g("even-or-odd", "Number Sort", SPEED, "Sort numbers at speed", EvenOdd()),
        g("arrow-stroop", "Stroop Lite", SPEED, "Trust the arrow, not the word", ArrowStroop(false)),
        g("go-no-go", "Go / No-Go", SPEED, "Hold back on the wrong shape", GoNoGo()),
        g("rapid-count", "Rapid Count", SPEED, "Count dots at a glance", RapidCount()),
        g("lightning-math", "Lightning Math", SPEED, "Sums against a tight clock", Arithmetic("+-", 0.55f)),

        // Executive function
        g("rule-shift", "Rule Shift", EXECUTIVE, "The rule keeps changing", RuleShift()),
        g("task-switcher", "Task Switcher", EXECUTIVE, "Swap between two questions", TaskSwitcher()),
        g("rule-keeper", "Rule Keeper", EXECUTIVE, "Follow the divisibility rule", RuleKeeper()),
        g("deadline-dash", "Deadline Dash", EXECUTIVE, "Plan around the clock", DeadlineDash()),
        g("reverse-reflex", "Reverse Reflex", EXECUTIVE, "Do the opposite", ArrowStroop(true)),

        // Problem solving
        g("symbol-algebra", "Symbol Algebra", PROBLEM, "Crack the value of each symbol", SymbolAlgebra()),
        g("balance-scale", "Balance Puzzle", PROBLEM, "Weigh shapes against each other", BalanceScale()),
        g("age-puzzle", "Age Puzzle", PROBLEM, "Word problems about ages", AgePuzzle()),
        g("change-maker", "Change Maker", PROBLEM, "Fewest coins wins", ChangeMaker()),
        g("pattern-breaker", "Pattern Breaker", PROBLEM, "Find what breaks the pattern", PatternBreaker()),

        // Visual reasoning
        g("symmetry-check", "Symmetry Check", VISUAL, "Same on both sides?", SymmetryCheck()),
        g("figure-completion", "Figure Completion", VISUAL, "Fill the missing cell", FigureCompletion()),
        g("growth-pattern", "Growth Pattern", VISUAL, "How does it grow?", GrowthPattern()),
        g("spot-difference", "Spot the Difference", VISUAL, "Find the mismatched pair", SpotDifference()),
        g("matching-pair", "Matching Pair", VISUAL, "Find the two identical sets", MatchingPair()),
    )

    private val byId = games.associateBy { it.id }
    fun find(id: String): GameDef? = byId[id]
}
