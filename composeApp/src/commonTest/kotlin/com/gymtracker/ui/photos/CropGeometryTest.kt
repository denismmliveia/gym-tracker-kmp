package com.gymtracker.ui.photos

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CropGeometryTest {

    private val rect = CropRectPx(left = 100f, top = 100f, right = 300f, bottom = 300f)
    private val bounds = CropRectPx(left = 0f, top = 0f, right = 500f, bottom = 500f)
    private val minSize = 80f
    private val touchTarget = 32f

    // findNearestHandle

    @Test
    fun `findNearestHandle returns TL for top-left corner`() {
        val handle = findNearestHandle(100f, 100f, rect, touchTarget)
        assertEquals(DragHandle.TL, handle)
    }

    @Test
    fun `findNearestHandle returns BR for bottom-right corner`() {
        val handle = findNearestHandle(300f, 300f, rect, touchTarget)
        assertEquals(DragHandle.BR, handle)
    }

    @Test
    fun `findNearestHandle returns T for top edge center`() {
        val handle = findNearestHandle(200f, 100f, rect, touchTarget)
        assertEquals(DragHandle.T, handle)
    }

    @Test
    fun `findNearestHandle returns R for right edge center`() {
        val handle = findNearestHandle(300f, 200f, rect, touchTarget)
        assertEquals(DragHandle.R, handle)
    }

    @Test
    fun `findNearestHandle returns null when far from all handles`() {
        val handle = findNearestHandle(200f, 200f, rect, touchTarget)
        assertNull(handle)
    }

    // applyDrag

    @Test
    fun `applyDrag TL moves left and top edges`() {
        val result = applyDrag(rect, DragHandle.TL, -10f, -10f, bounds, minSize)
        assertEquals(90f, result.left)
        assertEquals(90f, result.top)
        assertEquals(300f, result.right)
        assertEquals(300f, result.bottom)
    }

    @Test
    fun `applyDrag R moves only right edge`() {
        val result = applyDrag(rect, DragHandle.R, 20f, 0f, bounds, minSize)
        assertEquals(100f, result.left)
        assertEquals(100f, result.top)
        assertEquals(320f, result.right)
        assertEquals(300f, result.bottom)
    }

    @Test
    fun `applyDrag enforces minimum size on left edge`() {
        // Try to drag TL right edge past minSize limit
        val result = applyDrag(rect, DragHandle.TL, 250f, 0f, bounds, minSize)
        // left cannot go past right - minSize = 300 - 80 = 220
        assertEquals(220f, result.left)
    }

    @Test
    fun `applyDrag clamps to image bounds`() {
        // Try to drag TL above image bounds (top=0)
        val result = applyDrag(rect, DragHandle.TL, 0f, -200f, bounds, minSize)
        assertEquals(0f, result.top)
    }
}
