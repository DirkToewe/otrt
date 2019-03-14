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

import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.lang.Runtime.getRuntime
import java.lang.System.{nanoTime, err => cErr, out => cOut}
import java.nio.file.Files
import java.nio.file.Files.newOutputStream

import javax.imageio.ImageIO

object MainJVM
{
  def main( args: Array[String] ): Unit =
  {
    val world = Bunny.loadB64()//Bunny.loadFromFile()

    val dir = Files createTempDirectory("360° view ")
    for( yaw <- (0 until 360 by 1).par )
    {
      val img = new BufferedImage(800,600, TYPE_INT_ARGB)

      val canvas = CanvasJVM(img)
      val scene = new Scene(world)

      scene.camYaw    = yaw.toRadians
      scene.camPitch  =  10.toRadians
      scene.camFocusZ =  64

      val t0 = nanoTime()
      scene render canvas
      printf("dt: %.3f\n", {nanoTime() - t0} / 1e9)

      val path = dir resolve f"img_${yaw}%03d.png"
      val out = newOutputStream(path)
      try {
        ImageIO write (img, "png", out)
      }
      finally {
        out.close()
      }
    }

    println(dir.toString)
    for( cmd <- Seq(
      "ffmpeg -y -r 12 -start_number 0 -i img_%03d.png -c:v libvpx-vp9 -b:v 0 -crf 15 -pass 1 -an -f webm /dev/null",
      "ffmpeg -y -r 12 -start_number 0 -i img_%03d.png -c:v libvpx-vp9 -b:v 0 -crf 15 -pass 2 bunny360.webm",
      "ffmpeg -y -i bunny360.webm                       -vf fps=12,scale=400:-1:flags=lanczos,palettegen         gif_palette.png",
      "ffmpeg -y -i bunny360.webm -i gif_palette.png -lavfi fps=12,scale=400:-1:flags=lanczos[x];[x][1:v]paletteuse bunny360.gif"
    ) )
    {
      println("\n\n\n")
      val proc = getRuntime exec (cmd, Array.empty[String], dir.toFile)
      try {
        val pErr = proc.getErrorStream
        val pOut = proc.getInputStream
        val buf = new Array[Byte](1024)
        var sizeErr,
            sizeOut = 0
        while( sizeErr >= 0 || sizeOut >= 0 ) {
          sizeErr = pErr read buf; if( sizeErr > 0 ) { cErr write (buf, 0,sizeErr); cErr.flush() }
          sizeOut = pOut read buf; if( sizeOut > 0 ) { cOut write (buf, 0,sizeOut); cOut.flush() }
        }
        proc.waitFor()
      }
      finally {
        proc.destroy()
      }
    }
  }
}
