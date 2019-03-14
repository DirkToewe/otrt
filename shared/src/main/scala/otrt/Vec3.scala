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

import Math.{ sqrt => √ }

object Vec3 {
  def norm( x: Double, y: Double, z: Double ): Double =
  {
    val max = x.abs max
              y.abs max
              z.abs

    val u = x / max
    val v = y / max
    val w = z / max

    max * √(u*u + v*v + w*w)
  }

  def norm( xyz: (Double,Double,Double) ): Double
    = xyz match { case (x,y,z) => norm(x,y,z) }

  def dot( x: Double, y: Double, z: Double )( u: Double, v: Double, w: Double ): Double
    = x*u +
      y*v +
      z*w
}
