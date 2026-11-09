package com.mindforge101.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import com.mindforge101.app.domain.GameDef
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tiny drawing kit on a 24x24 grid. Every game icon is built from these primitives, so all 62
 * share stroke weight, corner radius and colour logic. [ac] is the category accent, [fg] a darker
 * shade of it for outlines and text.
 */
class Pen(
    private val s: DrawScope,
    private val u: Float,
    val fg: Color,
    val ac: Color,
    private val tm: TextMeasurer,
) {
    val white = Color.White
    val mid: Color = lerp(fg, Color.White, 0.4f)
    private fun o(x: Float, y: Float) = Offset(x * u, y * u)

    fun sq(x: Float, y: Float, w: Float, h: Float = w, fill: Color? = null, line: Color? = fg, r: Float = 1.4f, sw: Float = 1.6f) {
        if (fill != null) s.drawRoundRect(fill, o(x, y), Size(w * u, h * u), CornerRadius(r * u))
        if (line != null) s.drawRoundRect(line, o(x, y), Size(w * u, h * u), CornerRadius(r * u), style = Stroke(sw * u))
    }

    fun dot(x: Float, y: Float, r: Float, color: Color = ac) = s.drawCircle(color, r * u, o(x, y))
    fun ring(x: Float, y: Float, r: Float, color: Color = fg, sw: Float = 1.6f) =
        s.drawCircle(color, r * u, o(x, y), style = Stroke(sw * u))

    fun ln(x1: Float, y1: Float, x2: Float, y2: Float, color: Color = fg, sw: Float = 1.6f, dash: Boolean = false) =
        s.drawLine(
            color, o(x1, y1), o(x2, y2), sw * u, StrokeCap.Round,
            pathEffect = if (dash) PathEffect.dashPathEffect(floatArrayOf(1.8f * u, 2f * u)) else null,
        )

    fun poly(pts: List<Pair<Float, Float>>, fill: Color? = ac, line: Color? = null, sw: Float = 1.6f, close: Boolean = true) {
        val p = Path().apply {
            pts.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * u, y * u) else lineTo(x * u, y * u) }
            if (close) close()
        }
        if (fill != null) s.drawPath(p, fill)
        if (line != null) s.drawPath(p, line, style = Stroke(sw * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    fun tri(cx: Float, cy: Float, r: Float, fill: Color? = ac, line: Color? = null) =
        poly(listOf(cx to cy - r, cx + r * 0.95f to cy + r * 0.75f, cx - r * 0.95f to cy + r * 0.75f), fill, line)

    fun arrow(x1: Float, y1: Float, x2: Float, y2: Float, color: Color = fg, sw: Float = 1.8f) {
        ln(x1, y1, x2, y2, color, sw)
        val a = atan2(y2 - y1, x2 - x1)
        val h = 3.4f
        for (d in listOf(2.6f, -2.6f)) {
            ln(x2, y2, x2 + h * cos(a + d), y2 + h * sin(a + d), color, sw)
        }
    }

    fun arc(cx: Float, cy: Float, r: Float, start: Float, sweep: Float, color: Color = fg, sw: Float = 1.8f) =
        s.drawArc(color, start, sweep, false, o(cx - r, cy - r), Size(2 * r * u, 2 * r * u), style = Stroke(sw * u, cap = StrokeCap.Round))

    fun text(t: String, cx: Float, cy: Float, size: Float, color: Color = fg) {
        val style = TextStyle(color = color, fontSize = with(s) { (size * u).toSp() }, fontWeight = FontWeight.Bold)
        val layout = tm.measure(t, style)
        s.drawText(layout, topLeft = Offset(cx * u - layout.size.width / 2f, cy * u - layout.size.height / 2f))
    }

    fun check(cx: Float, cy: Float, color: Color = fg) {
        ln(cx - 3f, cy, cx - 1f, cy + 2.4f, color, 2.2f)
        ln(cx - 1f, cy + 2.4f, cx + 3.2f, cy - 2.6f, color, 2.2f)
    }

    fun cross(cx: Float, cy: Float, color: Color = fg) {
        ln(cx - 2.6f, cy - 2.6f, cx + 2.6f, cy + 2.6f, color, 2.2f)
        ln(cx - 2.6f, cy + 2.6f, cx + 2.6f, cy - 2.6f, color, 2.2f)
    }

    /** Three side-by-side tiles; tile [hi] is the highlighted answer slot. */
    fun seq(labels: List<String>, hi: Int = labels.lastIndex) {
        val w = 6.2f
        val gap = 1.4f
        val total = labels.size * w + (labels.size - 1) * gap
        val x0 = (24f - total) / 2f
        labels.forEachIndexed { i, l ->
            val x = x0 + i * (w + gap)
            if (i == hi) {
                sq(x, 8.5f, w, 7f, fill = ac, line = null)
                text(l, x + w / 2f, 12f, 5.2f, white)
            } else {
                sq(x, 8.5f, w, 7f, fill = white)
                text(l, x + w / 2f, 12f, 5.2f)
            }
        }
    }

    fun frac(cx: Float, top: String, bottom: String, color: Color = fg) {
        text(top, cx, 8.6f, 6f, color)
        ln(cx - 2.6f, 12f, cx + 2.6f, 12f, color, 1.4f)
        text(bottom, cx, 15.6f, 6f, color)
    }
}

object GameIcons {
    private val drawers: Map<String, Pen.() -> Unit> = buildMap {
        // ---- Memory
        put("memory-matrix") {
            sq(3.5f, 3.5f, 7f, fill = ac); sq(13.5f, 3.5f, 7f, fill = white)
            sq(3.5f, 13.5f, 7f, fill = white); sq(13.5f, 13.5f, 7f, fill = ac)
        }
        put("grid-recall") {
            for (i in 0..3) for (j in 0..3) {
                val lit = (i + 2 * j) % 3 == 0
                sq(2.6f + i * 5.1f, 2.6f + j * 5.1f, 4.2f, fill = if (lit) ac else white, r = 1f, sw = 1.2f)
            }
        }
        put("sequence-recall") {
            ln(7f, 7f, 17f, 7f, ac, 1.6f); ln(17f, 7f, 17f, 17f, ac, 1.6f)
            val pts = listOf(7f to 7f, 17f to 7f, 7f to 17f, 17f to 17f)
            pts.forEachIndexed { i, (x, y) -> dot(x, y, 3.8f, if (i == 3) white else ac); if (i == 3) ring(x, y, 3.8f); text("${i + 1}", x, y, 4.4f, if (i == 3) fg else white) }
        }
        put("pattern-pads") {
            val pts = listOf(5f to 8f, 12f to 8f, 19f to 8f, 19f to 16f, 12f to 16f, 5f to 16f)
            poly(pts.take(5), fill = null, line = ac, sw = 1.6f, close = false)
            pts.forEachIndexed { i, (x, y) -> dot(x, y, 2.9f, if (i < 5) ac else white); if (i == 5) ring(x, y, 2.9f, sw = 1.3f) }
        }
        put("number-memory") {
            sq(3.5f, 5f, 17f, 14f, fill = white); text("4 2", 12f, 12f, 7.5f)
            ln(7f, 16.2f, 17f, 16.2f, ac, 1.8f)
        }
        put("word-recall") {
            sq(4f, 3.5f, 16f, 17f, fill = white)
            ln(7.5f, 8.5f, 16.5f, 8.5f, fg, 1.8f); ln(7.5f, 12.5f, 16.5f, 12.5f, ac, 1.8f); ln(7.5f, 16.5f, 13f, 16.5f, fg, 1.8f)
        }
        put("symbol-recall") { sq(3.5f, 3.5f, 17f, fill = white); text("★", 12f, 12.4f, 11f, ac) }
        put("letter-recall") { sq(3.5f, 3.5f, 17f, fill = ac, line = null); text("A", 12f, 12.4f, 12f, white) }

        // ---- Focus
        put("odd-shape") {
            dot(7f, 7f, 3.6f); dot(17f, 7f, 3.6f); dot(7f, 17f, 3.6f)
            tri(17f, 17f, 4.2f, fill = null, line = fg)
        }
        put("odd-letter") {
            text("O", 7f, 7.5f, 8f, mid); text("O", 17f, 7.5f, 8f, mid); text("O", 7f, 17f, 8f, mid); text("Q", 17f, 17f, 8f, fg)
        }
        put("odd-digit") {
            text("8", 7f, 7.5f, 8f, mid); text("8", 17f, 7.5f, 8f, mid); text("8", 7f, 17f, 8f, mid); text("3", 17f, 17f, 8f, fg)
        }
        put("odd-arrow") {
            arrow(7f, 10f, 7f, 4.5f, ac); arrow(17f, 10f, 17f, 4.5f, ac)
            arrow(7f, 19.5f, 7f, 14f, ac); arrow(13.5f, 17f, 20f, 17f, fg)
        }
        put("target-count") {
            tri(7f, 8f, 3.8f); tri(16.5f, 6.5f, 3.2f); tri(12f, 17f, 4f); ring(19f, 17.5f, 2.2f); ring(5f, 18f, 2f)
        }
        put("letter-count") {
            text("A", 6f, 8f, 7f, mid); text("B", 12f, 8f, 7f); text("A", 18f, 8f, 7f, ac)
            text("B", 6f, 16f, 7f); text("A", 12f, 16f, 7f, mid); text("B", 18f, 16f, 7f)
        }

        // ---- Logic
        put("number-series") { seq(listOf("2", "4", "?")) }
        put("alternating-series") { seq(listOf("1", "7", "?")) }
        put("power-series") { seq(listOf("1", "4", "9")) }
        put("letter-series") { seq(listOf("A", "C", "?")) }
        put("pattern-next") {
            sq(1.8f, 8.5f, 6.2f, 7f, fill = white); sq(8.9f, 8.5f, 6.2f, 7f, fill = white); sq(16f, 8.5f, 6.2f, 7f, fill = ac, line = null)
            dot(4.9f, 12f, 1.8f); tri(12f, 12f, 2.1f, fill = fg); text("?", 19.1f, 12f, 5.2f, white)
        }
        put("odd-number-out") { seq(listOf("2", "4", "7"), hi = 2) }
        put("logic-chain") {
            ln(6f, 17f, 12f, 6.5f, fg, 1.6f); ln(12f, 6.5f, 18f, 17f, fg, 1.6f); ln(6f, 17f, 18f, 17f, fg, 1.6f)
            dot(12f, 6.5f, 3.4f); dot(6f, 17f, 3.4f, white); ring(6f, 17f, 3.4f); dot(18f, 17f, 3.4f)
        }

        // ---- Math
        put("quick-add") { ring(12f, 12f, 8.5f); ln(12f, 7.5f, 12f, 16.5f, ac, 2.6f); ln(7.5f, 12f, 16.5f, 12f, ac, 2.6f) }
        put("quick-subtract") { ring(12f, 12f, 8.5f); ln(7.5f, 12f, 16.5f, 12f, ac, 2.6f) }
        put("times-tables") { ring(12f, 12f, 8.5f); ln(8.5f, 8.5f, 15.5f, 15.5f, ac, 2.6f); ln(8.5f, 15.5f, 15.5f, 8.5f, ac, 2.6f) }
        put("mental-mix") {
            text("+", 7f, 7f, 9f, ac); text("−", 17f, 7f, 9f); text("×", 7f, 17f, 9f); text("÷", 17f, 17f, 9f, ac)
        }
        put("missing-number") {
            text("7 +", 7.5f, 12f, 7.5f)
            sq(13f, 7.5f, 8f, 9f, fill = ac, line = null); text("?", 17f, 12f, 6.5f, white)
        }
        put("missing-operator") { seq(listOf("3", "?", "4"), hi = 1) }
        put("estimate-it") { text("≈", 12f, 9.5f, 14f, ac); text("50", 12f, 18f, 6f) }
        put("fraction-match") { frac(5.5f, "1", "2", ac); text(">", 12f, 12f, 7f); frac(18.5f, "1", "3") }
        put("prime-hunter") { dot(12f, 12f, 8.5f); text("7", 12f, 12.2f, 10f, white) }

        // ---- Words
        put("word-scramble") {
            seq(listOf("T", "A", "C"), hi = -1)
            arrow(8f, 19f, 16f, 19f, ac, 1.5f)
        }
        put("anagram-sprint") {
            ln(2.5f, 8.5f, 6.5f, 8.5f, ac, 1.8f); ln(1.5f, 12f, 6.5f, 12f, ac, 1.8f); ln(2.5f, 15.5f, 6.5f, 15.5f, ac, 1.8f)
            sq(9f, 5.5f, 12f, 13f, fill = ac, line = null); text("A", 15f, 12.2f, 9f, white)
        }
        put("spelling-fix") { text("Abc", 11f, 10f, 8f); check(12f, 17.5f, ac) }
        put("synonym-match") { sq(2.5f, 7.5f, 7f, 9f, fill = white); sq(14.5f, 7.5f, 7f, 9f, fill = white); text("=", 12f, 12f, 8f, ac) }
        put("antonym-match") { sq(2.5f, 7.5f, 7f, 9f, fill = white); sq(14.5f, 7.5f, 7f, 9f, fill = ac, line = null); text("≠", 12f, 12f, 8f, fg) }
        put("category-sort") {
            dot(5f, 7f, 2.6f); dot(12f, 7f, 2.6f); dot(19f, 7f, 2.6f)
            tri(12f, 17f, 4f, fill = null, line = fg)
        }

        // ---- Spatial
        put("rotate-arrow") {
            sq(8f, 8f, 8f, fill = ac, line = null)
            arc(12f, 12f, 9f, -70f, 230f, fg, 1.8f)
            ln(3.54f, 15.08f, 2.64f, 18.4f, fg, 1.8f)
            ln(3.54f, 15.08f, 6.3f, 17f, fg, 1.8f)
        }
        put("mirror-match") {
            ln(12f, 3f, 12f, 21f, fg, 1.5f, dash = true)
            poly(listOf(4f to 18f, 9.5f to 6f, 9.5f to 18f), fill = ac)
            poly(listOf(20f to 18f, 14.5f to 6f, 14.5f to 18f), fill = null, line = fg)
        }
        put("compass-turns") {
            ring(12f, 12f, 9f)
            poly(listOf(12f to 4.5f, 15f to 12f, 9f to 12f), fill = ac)
            poly(listOf(12f to 19.5f, 15f to 12f, 9f to 12f), fill = null, line = fg, sw = 1.3f)
        }
        put("path-finder") {
            poly(listOf(5f to 19f, 5f to 11f, 12f to 11f, 12f to 5f, 19f to 5f), fill = null, line = ac, sw = 2.2f, close = false)
            dot(5f, 19f, 2.4f, fg); dot(19f, 5f, 2.8f)
        }
        put("distance-home") {
            ln(6f, 17f, 18f, 7f, ac, 1.8f, dash = true)
            dot(6f, 17f, 3.2f, fg); dot(18f, 7f, 3.2f)
        }

        // ---- Speed
        put("quick-compare") { text("9", 5f, 12f, 8.5f); text(">", 12f, 12f, 10f, ac); text("4", 19f, 12f, 8.5f) }
        put("even-or-odd") {
            sq(2.5f, 5.5f, 8.5f, 9f, fill = white); sq(13f, 5.5f, 8.5f, 9f, fill = ac, line = null)
            text("2", 6.7f, 10f, 6f); text("3", 17.2f, 10f, 6f, white)
            arrow(6.7f, 16.5f, 6.7f, 20.5f, fg, 1.6f); arrow(17.2f, 16.5f, 17.2f, 20.5f, ac, 1.6f)
        }
        put("arrow-stroop") {
            arrow(4f, 9.5f, 20f, 9.5f, ac, 3f)
            ln(6f, 17f, 18f, 17f, fg, 2.2f); ln(9f, 20.5f, 15f, 20.5f, fg, 2f)
        }
        put("go-no-go") {
            dot(7.5f, 12f, 5.2f); check(7.5f, 12f, white)
            ring(16.5f, 12f, 5.2f, fg, 1.8f); cross(16.5f, 12f, fg)
        }
        put("rapid-count") {
            dot(6f, 6f, 2f); dot(15f, 5f, 2f, fg); dot(11f, 10f, 2f); dot(19f, 11f, 2f)
            dot(5f, 14f, 2f, fg); dot(14f, 16f, 2f); dot(9f, 19.5f, 2f, fg); dot(19f, 19f, 2f)
        }
        put("lightning-math") {
            poly(listOf(13.5f to 2.5f, 6f to 13.5f, 11.5f to 13.5f, 9.5f to 21.5f, 18f to 9.5f, 12.5f to 9.5f), fill = ac)
            ln(18.5f, 16.5f, 18.5f, 21f, fg, 1.8f); ln(16.2f, 18.75f, 20.8f, 18.75f, fg, 1.8f)
        }

        // ---- Strategy / executive
        put("rule-shift") { arrow(3.5f, 8f, 20f, 8f, ac, 2.2f); arrow(20.5f, 16f, 4f, 16f, fg, 2.2f) }
        put("task-switcher") {
            sq(2.5f, 4.5f, 11f, 11f, fill = white)
            sq(10.5f, 9f, 11f, 11f, fill = ac, line = fg)
        }
        put("rule-keeper") { ring(12f, 12f, 9f); text("÷3", 12f, 12.2f, 8.5f, ac) }
        put("deadline-dash") {
            ln(1.5f, 9f, 4.5f, 9f, ac, 1.8f); ln(1.5f, 15f, 4.5f, 15f, ac, 1.8f)
            ring(14f, 12f, 8f); ln(14f, 12f, 14f, 7f, ac, 2f); ln(14f, 12f, 17.5f, 13.5f, ac, 2f)
        }
        put("reverse-reflex") {
            ln(7f, 19f, 7f, 11f, fg, 2f); arc(12f, 11f, 5f, 180f, 180f, fg, 2f)
            arrow(17f, 11f, 17f, 19f, ac, 2f)
        }

        // ---- Problem solving
        put("symbol-algebra") { text("★", 6.5f, 12.4f, 10f, ac); text("=", 12.8f, 12f, 7f); text("4", 18.8f, 12f, 8.5f) }
        put("balance-scale") {
            ln(12f, 5f, 12f, 19.5f, fg, 1.8f); ln(8f, 19.5f, 16f, 19.5f, fg, 1.8f)
            ln(4.5f, 9f, 19.5f, 6.5f, fg, 1.8f)
            ln(4.5f, 9f, 3f, 14f, fg, 1.2f); ln(4.5f, 9f, 6f, 14f, fg, 1.2f)
            ln(19.5f, 6.5f, 18f, 11.5f, fg, 1.2f); ln(19.5f, 6.5f, 21f, 11.5f, fg, 1.2f)
            arc(4.5f, 13.5f, 2.1f, 0f, 180f, ac, 2.4f); arc(19.5f, 11f, 2.1f, 0f, 180f, ac, 2.4f)
        }
        put("age-puzzle") {
            dot(12f, 8f, 3.6f); arc(12f, 22f, 8f, 200f, 140f, ac, 3f)
            text("?", 19f, 5.5f, 6f, fg)
        }
        put("change-maker") {
            dot(9f, 14f, 5.2f); text("¢", 9f, 14f, 6f, white)
            ring(15.5f, 13f, 5.2f, fg, 1.8f); ring(12f, 8f, 4.6f, ac, 1.6f)
        }
        put("pattern-breaker") {
            dot(4.5f, 12f, 2.6f); dot(10f, 12f, 2.6f); tri(15.5f, 12f, 3f, fill = null, line = fg); dot(20.5f, 12f, 2.6f)
        }

        // ---- Visual
        put("symmetry-check") {
            ln(12f, 3f, 12f, 21f, fg, 1.5f, dash = true)
            sq(4f, 5f, 6f, fill = ac, line = null); dot(7f, 16f, 2.6f)
            sq(14f, 5f, 6f, fill = null); ring(17f, 16f, 2.6f)
        }
        put("figure-completion") {
            sq(3f, 3f, 8.5f, fill = white); dot(7.25f, 7.25f, 2.4f)
            sq(12.5f, 3f, 8.5f, fill = white); tri(16.75f, 7.1f, 2.7f, fill = fg)
            sq(3f, 12.5f, 8.5f, fill = white); sq(5f, 14.5f, 4.5f, fill = ac, line = null)
            sq(12.5f, 12.5f, 8.5f, fill = null, line = ac); text("?", 16.75f, 16.9f, 6f, ac)
        }
        put("growth-pattern") {
            sq(2.5f, 15.5f, 3.5f, fill = ac, line = null, r = 1f); sq(7.5f, 13f, 6f, fill = ac, line = null, r = 1.2f); sq(14.5f, 10f, 9f, fill = ac, line = null)
            arrow(3f, 5f, 12f, 5f, fg, 1.5f)
        }
        put("spot-difference") {
            sq(2f, 5.5f, 9.5f, 13f, fill = white); dot(6.7f, 10f, 1.7f); dot(6.7f, 14.5f, 1.7f, fg)
            sq(12.5f, 5.5f, 9.5f, 13f, fill = white); dot(17.2f, 10f, 1.7f); dot(17.2f, 14.5f, 1.7f, ac); ring(17.2f, 14.5f, 3.2f, ac, 1.3f)
        }
        put("matching-pair") {
            sq(2.5f, 5f, 8f, 14f, fill = white); dot(6.5f, 12f, 2.6f)
            sq(13.5f, 5f, 8f, 14f, fill = white); dot(17.5f, 12f, 2.6f)
            ln(10.5f, 12f, 13.5f, 12f, ac, 2f)
        }
    }

    /** Ids that have a hand-drawn icon. A unit test checks this covers the whole catalog. */
    val ids: Set<String> get() = drawers.keys

    internal fun draw(id: String, pen: Pen): Boolean {
        val d = drawers[id] ?: return false
        pen.d()
        return true
    }
}

/** Soft pastel tile + category-coloured vector art that shows the game's mechanic. */
@Composable
fun GameIcon(game: GameDef, size: Dp, modifier: Modifier = Modifier) {
    val accent = MfColors.accent(game.category)
    val bg = lerp(accent, Color.White, 0.78f)
    val fg = lerp(accent, Color(0xFF14233D), 0.45f)
    val tm = rememberTextMeasurer()
    Canvas(modifier.size(size).clip(RoundedCornerShape(size * 0.29f))) {
        drawRect(bg)
        val u = this.size.minDimension / 24f
        val pen = Pen(this, u, fg, accent, tm)
        if (!GameIcons.draw(game.id, pen)) pen.text(tintFor(game.category).glyph, 12f, 12f, 11f, fg)
    }
}
