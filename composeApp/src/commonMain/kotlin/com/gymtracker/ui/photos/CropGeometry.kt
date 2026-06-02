package com.gymtracker.ui.photos

import kotlin.math.sqrt

data class CropRectPx(val left: Float, val top: Float, val right: Float, val bottom: Float)

enum class DragHandle { TL, T, TR, R, BR, B, BL, L }

fun findNearestHandle(px: Float, py: Float, rect: CropRectPx, touchTargetPx: Float): DragHandle? {
    val cx = (rect.left + rect.right) / 2f
    val cy = (rect.top + rect.bottom) / 2f
    val candidates = listOf(
        DragHandle.TL to Pair(rect.left,  rect.top),
        DragHandle.T  to Pair(cx,         rect.top),
        DragHandle.TR to Pair(rect.right, rect.top),
        DragHandle.R  to Pair(rect.right, cy),
        DragHandle.BR to Pair(rect.right, rect.bottom),
        DragHandle.B  to Pair(cx,         rect.bottom),
        DragHandle.BL to Pair(rect.left,  rect.bottom),
        DragHandle.L  to Pair(rect.left,  cy),
    )
    return candidates
        .map { (handle, pos) ->
            val dx = px - pos.first
            val dy = py - pos.second
            handle to sqrt(dx * dx + dy * dy)
        }
        .minByOrNull { it.second }
        ?.takeIf { it.second <= touchTargetPx }
        ?.first
}

fun applyDrag(
    rect: CropRectPx,
    handle: DragHandle,
    dx: Float,
    dy: Float,
    imageBounds: CropRectPx,
    minSizePx: Float
): CropRectPx {
    var l = rect.left; var t = rect.top; var r = rect.right; var b = rect.bottom
    when (handle) {
        DragHandle.TL -> { l += dx; t += dy }
        DragHandle.T  -> { t += dy }
        DragHandle.TR -> { r += dx; t += dy }
        DragHandle.R  -> { r += dx }
        DragHandle.BR -> { r += dx; b += dy }
        DragHandle.B  -> { b += dy }
        DragHandle.BL -> { l += dx; b += dy }
        DragHandle.L  -> { l += dx }
    }
    l = l.coerceIn(imageBounds.left, r - minSizePx)
    t = t.coerceIn(imageBounds.top,  b - minSizePx)
    r = r.coerceIn(l + minSizePx, imageBounds.right)
    b = b.coerceIn(t + minSizePx, imageBounds.bottom)
    return CropRectPx(l, t, r, b)
}
