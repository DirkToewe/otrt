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

sealed abstract class Node
{
  def height: Int
}
object Node
{
  def newSphere( lod: Int, color: Int = 0xFFFF0000 ): Node =
  {
    assert{ (color >>> 24) == 0xFF }
    assert( lod >= 0 )
    val size = 1 << lod
    val center = size * 0.5
    tabulate(size,size,size){
      (u,v,w) =>
        val x = u + 0.5
        val y = v + 0.5
        val z = w + 0.5
        val norm = Vec3.norm(
          x - center,
          y - center,
          z - center
        )
        if( norm > center )
          null
        else Leaf(
          color,
          x / norm,
          y / norm,
          z / norm
        )
    }
  }

  def tabulate( nx: Int, ny: Int, nz: Int )( voxel: (Int,Int,Int) => Leaf ): Node =
  {
    def tab( h: Int, x: Int, y: Int, z: Int ): Node =
    {
      if( x >= nx || y >= ny || z >= nz )
        Nil
      else if( 0 == h )
        voxel(x,y,z) match {
          case null => Nil
          case leaf => leaf
        }
      else {
        val off = 1 << h-1
        Branch(
          tab(h-1, x    , y    , z    ),
          tab(h-1, x    , y    , z+off),
          tab(h-1, x    , y+off, z    ),
          tab(h-1, x    , y+off, z+off),
          tab(h-1, x+off, y    , z    ),
          tab(h-1, x+off, y    , z+off),
          tab(h-1, x+off, y+off, z    ),
          tab(h-1, x+off, y+off, z+off)
        )
      }
    }

    val n = nx max ny max nz
    assert( n >= 0 )

    var       h=32
    if( {1 << h-16} >= n ) h -= 16
    if( {1 << h- 8} >= n ) h -=  8
    if( {1 << h- 4} >= n ) h -=  4
    if( {1 << h- 2} >= n ) h -=  2
    if( {1 << h- 1} >= n ) h -=  1

    assert( h < 32 )
//    assert( {1 << h  } >= n )
//    assert( {1 << h-1} <  n )

    tab(h,0,0,0)
  }
}
final case class Leaf( color: Int, nx: Double, ny: Double, nz: Double ) extends Node
{
  override def height = 0
  assert{ (color >>> 24) == 0xFF }
}
final case object Nil extends Node
{
  override def height = ???
}
final class Branch private( val c000: Node, val c001: Node, val c010: Node, val c011: Node,
                            val c100: Node, val c101: Node, val c110: Node, val c111: Node ) extends Node
{
  override val height= {
    var height = -1
    def getHeight( child: Node )
      = child match {
          case Nil =>
          case node =>
            val h = node.height
            assert( 0 > height || height == h )
            height = h
        }
    getHeight(c000)
    getHeight(c001)
    getHeight(c010)
    getHeight(c011)
    getHeight(c100)
    getHeight(c101)
    getHeight(c110)
    getHeight(c111)
    assert( height >= 0 )
    height+1
  }

  def child( index: Int ): Node
    = index match {
        case 0 => c000
        case 1 => c001
        case 2 => c010
        case 3 => c011
        case 4 => c100
        case 5 => c101
        case 6 => c110
        case 7 => c111
      }
}
object Branch
{
  def apply( c000: Node, c001: Node, c010: Node, c011: Node,
             c100: Node, c101: Node, c110: Node, c111: Node ): Node =
  {
    assert{ null != c000 }
    assert{ null != c001 }
    assert{ null != c010 }
    assert{ null != c011 }
    assert{ null != c100 }
    assert{ null != c101 }
    assert{ null != c110 }
    assert{ null != c111 }
    if( c000 == Nil && c001 == Nil &&
        c010 == Nil && c011 == Nil &&
        c100 == Nil && c101 == Nil &&
        c110 == Nil && c111 == Nil )
      Nil
    else
      new Branch(
        c000, c001, c010, c011,
        c100, c101, c110, c111
      )
  }
}
