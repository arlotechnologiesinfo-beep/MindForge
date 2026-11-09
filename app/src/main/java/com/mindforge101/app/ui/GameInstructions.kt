package com.mindforge101.app.ui

import com.mindforge101.app.domain.GameDef

/** Presentation-only how-to-play text for the intro screen. Does not affect game logic. */
object GameInstructions {
    private val text: Map<String, String> = mapOf(
        // Memory
        "memory-matrix" to "A few squares light up on a 3×3 grid. Remember them, then tap the same squares.",
        "grid-recall" to "Same idea on a bigger 4×4 grid. Watch which squares are marked, then tap them from memory.",
        "sequence-recall" to "Four pads flash in an order. Repeat the same order by tapping them.",
        "pattern-pads" to "Six pads flash in a longer order. Tap them back in the exact same order.",
        "number-memory" to "A number appears briefly. When it disappears, type the number you saw.",
        "word-recall" to "You see a list of words for a moment. Then pick the word that was on the list.",
        "symbol-recall" to "Study the symbols, then pick the one that was shown.",
        "letter-recall" to "Study the letters, then pick the one that was shown.",
        // Focus
        "odd-shape" to "Scan the grid and tap the shape that is different from the rest.",
        "odd-letter" to "Scan the grid and tap the letter that is different from the rest.",
        "odd-digit" to "Scan the grid and tap the digit that is different from the rest.",
        "odd-arrow" to "Scan the grid and tap the arrow that points a different way.",
        "target-count" to "Count how many triangles are mixed in with the other shapes, then choose your answer.",
        "letter-count" to "Count how many times one letter appears in the crowd, then choose your answer.",
        // Logic
        "number-series" to "Find the rule in the number series (adding or multiplying) and choose what comes next.",
        "alternating-series" to "Two rules take turns in this series. Work out both and choose the next number.",
        "power-series" to "Squares, triangular numbers and sums. Spot the pattern and choose the next number.",
        "letter-series" to "Find the pattern in the letters and choose which letter comes next.",
        "pattern-next" to "Look at the shape pattern and choose which shape comes next.",
        "odd-number-out" to "Four numbers follow a rule and one does not. Pick the one that doesn't belong.",
        "logic-chain" to "Read the clues, link them together and work out who the answer is.",
        // Math
        "quick-add" to "Solve the addition problem and choose the right answer before time runs out.",
        "quick-subtract" to "Solve the subtraction problem and choose the right answer before time runs out.",
        "times-tables" to "Solve the multiplication in your head and choose the right answer.",
        "mental-mix" to "Addition, subtraction and multiplication mixed together. Solve each one in your head.",
        "missing-number" to "One number is missing from the equation. Work out what fills the gap.",
        "missing-operator" to "Choose the sign (+, −, ×) that makes the equation true.",
        "estimate-it" to "No need to be exact. Choose the closest estimate by rounding in your head.",
        "fraction-match" to "Two fractions are shown. Tap the one that is larger.",
        "prime-hunter" to "Among the numbers shown, find the prime number.",
        // Words
        "word-scramble" to "The letters are mixed up. Choose the word they spell when unscrambled.",
        "anagram-sprint" to "Longer words against a shorter clock. Unscramble the letters quickly.",
        "spelling-fix" to "Several spellings are shown. Choose the one that is spelled correctly.",
        "synonym-match" to "Choose the word that means the same as the word shown.",
        "antonym-match" to "Choose the word that means the opposite of the word shown.",
        "category-sort" to "Most of these words belong together. Pick the one that doesn't fit.",
        // Spatial
        "rotate-arrow" to "Picture the shape turning in your head, then choose how it looks after the rotation.",
        "mirror-match" to "Imagine the shape mirrored or flipped and choose the matching result.",
        "compass-turns" to "Follow the turns you are given and choose which way you face now.",
        "path-finder" to "Follow the list of moves step by step and choose where you end up.",
        "distance-home" to "Follow the moves, then work out how far you are from where you started.",
        // Speed
        "quick-compare" to "Two numbers appear. Tap the larger one as fast as you can.",
        "even-or-odd" to "A number appears with a rule. Answer Yes or No quickly.",
        "arrow-stroop" to "Ignore the word and follow the arrow. Which way does the arrow point?",
        "go-no-go" to "React quickly to the right target, and hold back on the wrong one.",
        "rapid-count" to "Dots flash on screen. Count them at a glance and choose the number.",
        "lightning-math" to "Quick sums against a tight clock. Answer fast and stay accurate.",
        // Strategy
        "rule-shift" to "The rule changes between questions. Read it each time and answer by the current rule.",
        "task-switcher" to "You switch between two kinds of questions. Check which one you are being asked.",
        "rule-keeper" to "Follow the divisibility rule shown and answer for each number.",
        "deadline-dash" to "Several tasks have deadlines. Decide which is due first or can wait longest.",
        "reverse-reflex" to "Do the opposite. Tap the direction that is opposite to the arrow.",
        // Problem solving
        "symbol-algebra" to "Each symbol stands for a number. Work out its value from the clues.",
        "balance-scale" to "Weigh shapes against each other on the scale and work out the answer.",
        "age-puzzle" to "A short word problem about ages. Work out the answer and choose it.",
        "change-maker" to "Find the fewest coins needed to make the amount shown.",
        "pattern-breaker" to "One position breaks the pattern. Choose which one.",
        // Visual
        "symmetry-check" to "Look at the figure and decide whether it is the same on both sides.",
        "figure-completion" to "One cell of the figure is missing. Choose what fills it.",
        "growth-pattern" to "A pattern grows row by row. Work out how many are in the next row.",
        "spot-difference" to "Several pairs are shown and one pair doesn't match. Pick it.",
        "matching-pair" to "Two of the sets are identical. Find the matching pair.",
    )

    /** Falls back to the catalog blurb if a game has no dedicated text. */
    fun forGame(game: GameDef): String = text[game.id] ?: game.blurb
    val ids: Set<String> get() = text.keys
}
