/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.util.math

import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.ceil

class Rectangle constructor(x: Int = 0, y: Int = 0, width: Int, height: Int) {

    companion object {
        fun fromRectangle(rect: Rectangle): Rectangle{
            return Rectangle(rect.x0, rect.y0, rect.width, rect.height)
        }
    }

    // All variables have custom setters that will update the tied variables (x1, y1)
    var x0 = x
        set(newX) {
            field = newX
            x1 = newX + width
        }
    var y0 = y
        set(newY) {
            field = newY
            y1 = newY + height
        }
    var width = width
        set(newWidth) {
            field = newWidth
            x1 = x0 + newWidth
        }
    var height = height
        set(newHeight) {
            field = newHeight
            y1 = y0 + newHeight
        }

    //
    var x1: Int = x0 + width
        private set
    var y1: Int = y0 + height
        private set

    // Named getters
    val left get() = x0
    val right get() = x1
    val top get() = y0
    val bottom get() = y1

    // Constructors
    constructor() : this(0, 0, 0, 0)
    constructor(width: Int, height: Int) : this(0, 0, width, height)
    private constructor(firstPoint: Pair<Int, Int>, secondPoint: Pair<Int, Int>) : this(firstPoint.first, firstPoint.second, (secondPoint.first - firstPoint.first), (secondPoint.second - firstPoint.second))

    // Transformations
    fun setSize(newWidth: Int = width, newHeight: Int = height): Rectangle {
        width = newWidth
        height = newHeight
        return this
    }

    fun setSizeFromRectangle(rect: Rectangle): Rectangle{
        setSize(rect.width, rect.height)
        return this
    }

    fun setPos(newX: Int = x0, newY: Int = y0): Rectangle {
        x0 = newX
        y0 = newY
        return this
    }

    fun setPosFromRectangle(rect: Rectangle): Rectangle{
        setPos(rect.x0, rect.y0)
        return this
    }

    fun scale(fMultiplier: Float): Rectangle = scale(fMultiplier.toDouble())
    fun scale(dMultiplier: Double): Rectangle {
        width = ceil(dMultiplier * width).toInt()
        height = ceil(dMultiplier * height).toInt()
        return this
    }

    fun grow(growX: Int, growY: Int): Rectangle {
        width += growX
        height += growY
        return this
    }

    fun translate(translateX: Int, translateY: Int): Rectangle {
        x0 += translateX
        y0 += translateY
        return this
    }

    // Non-destructive transformations
    fun translated(translateX: Int, translateY: Int): Rectangle {
        return fromRectangle(this).translate(translateX, translateY)
    }
    fun translated(rect: Rectangle): Rectangle{
        return fromRectangle(this).translate(rect.x0, rect.y0)
    }

    // Checks
    fun contains(x: Int, y: Int): Boolean = x in x0..x1 && y in y0..y1
    fun containsAny(vararg coords: Pair<Int, Int>): Boolean = coords.any { contains(it.first, it.second) }
    fun containsAll(vararg coords: Pair<Int, Int>): Boolean = coords.all { contains(it.first, it.second) }
    fun contains(rect: Rectangle): Boolean = contains(rect.x0, rect.y0) && contains(rect.x1, rect.y1)
    fun intersects(rect: Rectangle): Boolean = !(right < rect.left || rect.right < left || bottom < rect.top || rect.bottom < top)

    // Operations
    fun overlap(rect: Rectangle): Rectangle = Rectangle(max(this.x0, rect.x0) to max(this.y0, rect.y0), min(this.x1, rect.x1) to min(this.y1, rect.y1))

    override fun toString(): String {
        return "Rectangle(x = $x0, y = $y0, width = $width, height = $height)"
    }
}