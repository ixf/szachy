package brt.szachy.shared.logic

case class Direction(dx: Int, dy: Int){
  def apply(p: Position): Position = Position(p.x+dx, p.y+dy)
  def apply(d: Direction): Direction = {
    val ox = dx
    val oy = dy
    Direction(ox+d.dx, oy+d.dy)
  }
  def negate(): Direction = Direction(-dx, -dy)
}

