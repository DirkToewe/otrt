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

import Math.{cos, hypot, sin}

class Scene( val world: Node )
{
  private val childOrder = new Array[Byte](8)

  var camYaw    = 45.0d.toRadians
  var camPitch  = 45.0d.toRadians
  var camDist   = 20.0d
  var camFocusX =  0.0d
  var camFocusY =  0.0d
  var camFocusZ =  0.0d

  private val (lightX,
               lightY,
               lightZ) = {
    val x = +1
    val y = +2
    val z = -4
    val  norm = Vec3.norm(x,y,z)
    (x / norm,
     y / norm,
     z / norm)
  }

  def render( canvas: Canvas ): Unit =
  {
    import canvas.{ nRows, nCols }

    childOrder(0) = 0
    childOrder(1) = 1
    childOrder(2) = 2
    childOrder(3) = 3
    childOrder(4) = 4
    childOrder(5) = 5
    childOrder(6) = 6
    childOrder(7) = 7

    val cosPitch = cos(camPitch)
    val sinPitch = sin(camPitch)

    val cosYaw = cos(camYaw)
    val sinYaw = sin(camYaw)

    val dirX = +cosPitch * cosYaw
    val dirY = +cosPitch * sinYaw
    val dirZ = +sinPitch

    val rightX = +sinYaw
    val rightY = -cosYaw
    val rightZ = 0

    val downX = +sinPitch * cosYaw
    val downY = +sinPitch * sinYaw
    val downZ = -cosPitch

    assert( {Vec3.norm(  dirX,  dirY,  dirZ) - 1} < 1e-7 )
    assert( {Vec3.norm( downX, downY, downZ) - 1} < 1e-7 )
    assert( {Vec3.norm(rightX,rightY,rightZ) - 1} < 1e-7 )

    assert( Vec3.dot(  dirX,  dirY,  dirZ)(rightX,rightY,rightZ).abs < 1e-7 )
    assert( Vec3.dot(  dirX,  dirY,  dirZ)( downX, downY, downZ).abs < 1e-7 )
    assert( Vec3.dot(rightX,rightY,rightZ)( downX, downY, downZ).abs < 1e-7 )

    val size = 1 << world.height
    val ratio = 0.85 * size / (nRows min nCols)

    def swap( i: Int, j: Int ): Unit = {
      val tmp = childOrder(i)
                childOrder(i) = childOrder(j)
                                childOrder(j) = tmp
    }

    if( dirX >= 0 ) { swap(0,4); swap(1,5); swap(2,6); swap(3,7) }
    if( dirY >= 0 ) { swap(0,2); swap(1,3); swap(4,6); swap(5,7) }
    if( dirZ >= 0 ) { swap(0,1); swap(2,3); swap(4,5); swap(6,7) }

    def traceRay( node: Node, posX: Double, posY: Double, posZ: Double ): Int
      = node match {
          case Nil =>
            0
          case _ if {
            // check if the ray misses node's bounding box
            val size = (1 << node.height) * 0.5
               ( dirX == 0 || { val s = (posX + size*dirX.signum) / dirX; (posY - s*dirY).abs > size || (posZ - s*dirZ).abs > size } )
            .&&( dirY == 0 || { val s = (posY + size*dirY.signum) / dirY; (posZ - s*dirZ).abs > size || (posX - s*dirX).abs > size } )
            .&&( dirZ == 0 || { val s = (posZ + size*dirZ.signum) / dirZ; (posX - s*dirX).abs > size || (posY - s*dirY).abs > size } )
          } => 0
          case Leaf(color, nx, ny, nz) =>
            val dot = (1 - lightX*nx - lightY*ny - lightZ*nz) / 2 max 0 min 1
            return {(0xFF & color >>> 16)*dot}.toInt << 16 |
                   {(0xFF & color >>>  8)*dot}.toInt <<  8 |
                   {(0xFF & color >>>  0)*dot}.toInt <<  0 | 0xFF000000
          case branch: Branch =>
            val off = (1 << node.height) * 0.25
            var    i = 8
            while( i > 0 ) {
                   i-= 1
              val ci = childOrder(i)
              val color = traceRay(
                branch child ci,
                posX + off*( (ci>>>1 & 2) - 1 ),
                posY + off*( (ci     & 2) - 1 ),
                posZ + off*( (ci<< 1 & 2) - 1 )
              )
              val alpha = color >>> 24
              if( alpha == 0xFF )
                return color
              assert( alpha == 0 )
            }
            0
        }

    val posX = camFocusX /*- dirX * camDist*/ - downX*ratio*nRows/2 - rightX*ratio*nCols/2
    val posY = camFocusY /*- dirY * camDist*/ - downY*ratio*nRows/2 - rightY*ratio*nCols/2
    val posZ = camFocusZ /*- dirZ * camDist*/ - downZ*ratio*nRows/2 - rightZ*ratio*nCols/2

    ;{
      var row=0; while( row < nRows ) {
      var col=0; while( col < nCols ) {

        canvas(row,col) = 0xFF000000 | traceRay(
          world,
          posX + downX*ratio*row + rightX*ratio*col,
          posY + downY*ratio*row + rightY*ratio*col,
          posZ + downZ*ratio*row + rightZ*ratio*col
        )

      col += 1 }
      row += 1 }
    }
  }
}
object Scene
{
}
