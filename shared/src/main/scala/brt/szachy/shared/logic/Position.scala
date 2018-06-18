package brt.szachy.shared.logic

sealed case class Position(x: Int, y: Int) {
  import Math.abs

  def apply(d: Direction) = d(this)

  def +(other: Position) = Position(x+other.x, y+other.y)

  def tuple = (x,y)
  def strTuple = (x.toString, y.toString)

  def onBoard: Boolean = (x>=0) && (x<8) && (y>=0) && (y<8)
  def samePlus(to: Position) = to.y == y || to.x == x
  def sameDiag(to: Position) = abs(x-to.x) == abs(y-to.y)
  def delta(to: Position) = (x-to.x, y-to.y)
  def absDelta(to: Position) = (abs(x-to.x), abs(y-to.y))
  def incAbsDelta(to: Position) = {
    val (x,y) = absDelta(to)
    if (x > y) (y,x) else (x,y)
  }

  def distance(to: Position) = {
    val a = absDelta(to)
    a._1 + a._2
  }

}

object Position {
}

