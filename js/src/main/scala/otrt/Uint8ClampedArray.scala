/* This file is part of OctTreeRayTracer.
 *
 * OctTreeRayTracer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OctTreeRayTracer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OctTreeRayTracer.  If not, see <https://www.gnu.org/licenses/>.
 */

package otrt

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSBracketAccess, JSGlobal}

@js.native
@JSGlobal
class Uint8ClampedArray private[this]() extends js.Any
{
  def this( length: Int ) = this()
  def this( length: Uint8ClampedArray ) = this()

  val length: Int = js.native

  @JSBracketAccess def  apply( index: Int ): Int = js.native
  @JSBracketAccess def update( index: Int, value: Double ): Unit = js.native

  def map( mapFn: js.Function1[Int,    Double] ): Uint8ClampedArray = js.native
  def map( mapFn: js.Function2[Int,Int,Double] ): Uint8ClampedArray = js.native

  def every( predicate: js.Function1[Int,    Boolean] ): Boolean = js.native
  def every( predicate: js.Function2[Int,Int,Boolean] ): Boolean = js.native

  def fill( value: Double, from: Int=0, until: Int=length ): Uint8ClampedArray = js.native

  def reduce[R]( reduceFn: js.Function2[R,Double,R], initialValue: Int = ??? ): R = js.native

  def slice( from: Int, until: Int=length ): Uint8ClampedArray = js.native
}
@js.native
@JSGlobal
object Uint8ClampedArray extends js.Any
{
  def of( values: Double* ): Uint8ClampedArray = js.native

  def from( source: Uint8ClampedArray, mapFn: js.Function1[Double,    Double] ): Uint8ClampedArray = js.native
  def from( source: Uint8ClampedArray, mapFn: js.Function2[Double,Int,Double] ): Uint8ClampedArray = js.native
}
