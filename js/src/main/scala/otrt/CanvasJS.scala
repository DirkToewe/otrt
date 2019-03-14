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

class CanvasJS private(
  override val nRows: Int,
  override val nCols: Int,
  data: Uint8ClampedArray
) extends Canvas
{
  assert( data.length == 4*nRows*nCols )

  override def update( row: Int, col: Int, rgba: Int ) =
  {
    assert( 0 <= row )
    assert( 0 <= col )
    assert(      row < nRows )
    assert(      col < nCols )

    val i = row*nCols + col  <<  2

    data(i+0) = 0xFF  &  rgba >>>  0
    data(i+1) = 0xFF  &  rgba >>>  8
    data(i+2) = 0xFF  &  rgba >>> 16
    data(i+3) = 0xFF  &  rgba >>> 24
  }
}
object CanvasJS
{
  def apply( nRows: Int, nCols: Int, data: Uint8ClampedArray )
   = new CanvasJS(nRows,nCols,data)
}
