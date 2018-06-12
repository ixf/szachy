package brt.szachy.shared.logic

sealed trait Direction {
  def apply(p: Position): Position = Position(p.x+dx, p.y+dy)
  def apply(d: Direction): Direction = {
    val ox = dx
    val oy = dy
    new Direction {
      val dx = ox+d.dx
      val dy = oy+d.dy
    } 
  }
  val dx: Int
  val dy: Int
}

case object up extends Direction {
  val dx = 0
  val dy = -1
}

case object down extends Direction {
  val dx = 0
  val dy = 1
}

case object left extends Direction {
  val dx = -1
  val dy = 0
}
case object right extends Direction {
  val dx = 1
  val dy = 0
}

