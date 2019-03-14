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

import org.scalajs.dom.raw.HTMLCanvasElement
import org.scalajs.dom.{ document => doc }

import scalajs.js.timers.setInterval

object MainJS
{
  def main( args: Array[String] ): Unit =
  {
    val htmlCanvas = doc.getElementById("mainCanvas").asInstanceOf[HTMLCanvasElement]
    val nCols: Int = htmlCanvas.width
    val nRows: Int = htmlCanvas.height
    val c2d = htmlCanvas getContext "2d"
    c2d.imageSmoothingEnabled = false

    val imgDat = c2d createImageData (nCols,nRows)
    val canvas = CanvasJS(nRows,nCols, imgDat.data.asInstanceOf[Uint8ClampedArray])
    val scene = new Scene( Bunny.loadB64() ) // let's make a scene... sorry for the obscene joke

    scene.camPitch  = 10.toRadians
    scene.camFocusZ = 64

    setInterval(32){
      scene.camYaw += 2.toRadians
      scene render canvas
      c2d putImageData (imgDat, 0,0)
    }
  }
}
