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

import java.io.BufferedInputStream
import java.nio.file.FileSystems.newFileSystem
import java.nio.file.Files._
import java.nio.file.{Path, Paths}
import java.nio.file.StandardOpenOption.READ
import java.util.Base64

import scala.collection.mutable.ArrayBuffer

/** Voxel data from the Stanford Bunny CT Scan
  * https://graphics.stanford.edu/data/voldata/
  */
object Bunny
{
  def main( args: Array[String] ): Unit =
  {
    val str = buildB64()
    val path = createTempFile("bunny ",".txt")
    val writer = newBufferedWriter(path)

    val lines = str.grouped(1024).toArray

    writer write "lazy val value = Array(\n"
    for( i <- 0 until lines.length )
      writer.write(s"l$i,")
    writer write "\n) mkString \"\""

    try {
      for( (line,i) <- lines.zipWithIndex ) {
        writer write s"private def l"
        writer write i.toString
        writer write " = \""
        writer write line
        writer write "\"\n"
      }
    }
    finally {
      writer.close()
    }

    println( path.toString )
  }

  private def buildB64(): String =
  {
    val layers = loadRaw()
    val buf = ArrayBuffer.empty[Byte]

    var x=0; while( x < layers.length ) {
    var y=0; while( y < 512 ) {
    var z=0; while( z < 512 ) {
      if( isVoxel(layers)(x,y,z) ) {
        buf += x.toByte
        buf += y.toByte
        buf += z.toByte
        buf += (
          (x >>> 8 & 1) |
          (y >>> 7 & 2) |
          (z >>> 6 & 4)
        ).toByte

        val nx = layers(x-1)(y)(z) - layers(x+1)(y)(z)
        val ny = layers(x)(y-1)(z) - layers(x)(y+1)(z)
        val nz = layers(x)(y)(z-1) - layers(x)(y)(z+1)
        val n = nx.abs max ny.abs max nz.abs

        buf += (nx*127 / n).toByte
        buf += (ny*127 / n).toByte
        buf += (nz*127 / n).toByte
      }
    z += 1 }
    y += 1 }
    x += 1 }

    Base64.getEncoder encodeToString buf.toArray
  }

  private def isVoxel( layers: Array[Array[Array[Int]]] )( x: Int, y: Int, z: Int ) =
  {
    val NX = layers.length
    val NY = 512
    val NZ = 512

    def sparse( x: Int,
                y: Int,
                z: Int ) = {
      val density = layers(x)(y)(z)
      density < 1024+512+128 ||
      density >= (1<<12)
    }
    ! sparse(x,y,z) && (
      x == 0 || x == NX-1 || sparse(x-1,y,z) || sparse(x+1,y,z) ||
      y == 0 || y == NY-1 || sparse(x,y-1,z) || sparse(x,y+1,z) ||
      z == 0 || z == NZ-1 || sparse(x,y,z-1) || sparse(x,y,z+1)
    )
  }

  def loadB64(): Node
    = parseB64(BunnyB64.value)

  private def parseB64( B64: String ): Node =
  {
    val leafs = Array.fill[Leaf](361,512,512)(null)
    val buf = Base64.getDecoder.decode(B64).iterator

    while( buf.hasNext )
    {
      var x,y,z = buf.next() & 0xFF
      val xyz   = buf.next()
      x |= (xyz & 1) << 8
      y |= (xyz & 2) << 7
      z |= (xyz & 4) << 6

      val nx = buf.next()
      val ny = buf.next()
      val nz = buf.next()
      val norm = Vec3 norm (nx,ny,nz)
      leafs(x)(y)(z) = Leaf(
        0xFFFFFFFF,
        nx / norm,
        ny / norm,
        nz / norm
      )
    }
    val NX = leafs.length
    val NY = leafs(0).length
    val NZ = leafs(0)(0).length

    Node.tabulate(NZ,NY,NX){ (z,y,x) => leafs(x)(y)(z) }
  }

  def loadFromFile(): Node =
  {
    val layers = loadRaw()
    val NX = layers.length
    val NY = layers(0).length
    val NZ = layers(0)(0).length

    Node.tabulate(NZ,NY,NX){
      (z,y,x) =>
        if( ! isVoxel(layers)(x,y,z) )
          null
        else {
          // NORMAL COMPUTATION METHOD 1: use density gradient as normal
          val nx = layers(x-1)(y)(z) - layers(x+1)(y)(z)
          val ny = layers(x)(y-1)(z) - layers(x)(y+1)(z)
          val nz = layers(x)(y)(z-1) - layers(x)(y)(z+1)

//          // NORMAL COMPUTATION METHOD 2: normal points toward lowest density voxel
//          var minDensity = Double.PositiveInfinity
//          var nx,ny,nz = 0
//          for( i <- -1 to +1 )
//          for( j <- -1 to +1 )
//          for( k <- -1 to +1 )
//            if( i != 0 || j != 0 || k != 0 ) {
//              val d = layers(x+i)(y+j)(z+k)
//              if( minDensity > d ) {
//                  minDensity = d
//                nx = i
//                ny = j
//                nz = k
//              }
//            }

          val norm = Vec3 norm (nx,ny,nz)
          Leaf(
            0xFFFFFFFF,
            nx / norm,
            ny / norm,
            nz / norm
          )
        }
    }
  }

  private def loadRaw(): Array[Array[Array[Int]]] = {
    val home = System getProperty "user.home"
    val fs = newFileSystem( Paths.get(home, "Documents/bunny.zip"), null )
    val NumberFile = """(\d+)""".r // <- FIXME i was told there is an inline method for regex in pattern matching

    try {
      fs.getPath("/bunny")
        .filterNot{ isDirectory(_) }
        .map( file => (file.getFileName.toString, file) )
        .collect{
           case (NumberFile(num),file) => (num.toInt,file)
         }
        .toParArray
        .map{
           case (iLayer,file) =>
             assert( size(file) == 2*512*512 )
             val layer = Array.fill(512,512)(0)
             val is = new BufferedInputStream( newInputStream(file,READ), 2*512 )
             try {
               for( x <- 0 until 512 )
               for( y <- 0 until 512 )
                 layer(x)(y) = is.read().&(0xFF) << 8 | is.read().&(0xFF)
               assert( 0 > is.read() )
             }
             finally { is.close() }

             iLayer -> layer
         }
        .toArray
        .sortBy(_._1)
        .map(_._2)
    }
    finally {
      fs.close()
    }
  }

  private implicit class TraversableDirectory( val dir: Path ) extends Traversable[Path]
  {
    if( ! isDirectory(dir) )
      throw new IllegalArgumentException

    override def foreach[U]( consumer: Path => U )
      = newDirectoryStream(dir) forEach { path => consumer(path) }
  }
}
