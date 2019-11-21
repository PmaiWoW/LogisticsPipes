/*
 * Copyright (c) 2019  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2019  RS485
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

package network.rs485.logisticspipes.pipe

import net.minecraft.block.BlockState
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Util
import network.rs485.logisticspipes.init.Registries
import network.rs485.logisticspipes.pipe.shape.PipeShape
import network.rs485.logisticspipes.transport.Pipe

abstract class PipeType<X, out T : Pipe<*, X>, in I> {

    private var translationKey: String? = null

    abstract fun create(itf: I): T

    abstract fun getBaseShape(state: BlockState): PipeShape<X>

    protected fun getOrCreateTranslationKey(): String {
        return translationKey ?: run {
            val key = Util.createTranslationKey("pipe", Registries.PipeType.getId(this));
            translationKey = key
            key
        }
    }

    open fun getTranslationKey(): String {
        return getOrCreateTranslationKey()
    }

    open fun getName(): Text {
        return TranslatableText(getTranslationKey())
    }

    override fun toString(): String = getName().asString()

    class Builder<X, T : Pipe<*, X>, I>(private val shape: (BlockState) -> PipeShape<X>, private val constructor: (I) -> T) {
        fun build(): PipeType<X, T, I> = object : PipeType<X, T, I>() {
            override fun create(itf: I): T = constructor(itf)

            override fun getBaseShape(state: BlockState): PipeShape<X> = shape(state)
        }
    }

}