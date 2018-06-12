package brt.szachy.shared.logic

object Pieces {
  def apply() = {
    val order = Array(Rook, Knight, Bishop, Queen, King, Bishop, Knight, Rook)
    
    new Pieces( Vector.tabulate(8,8){ (i,j) =>
      val color = if (j < 4) Black else White
      j match {
        case 1 => Some(Piece(color, Pawn))
        case 6 => Some(Piece(color, Pawn))
        case 0 => Some(Piece(color, order(7-i)))
        case 7 => Some(Piece(color, order(i)))
        case _ => None
      }
    })
  }
}


case class Pieces(contents: Vector[Vector[Option[Piece]]]){
  def get(p: Position) = if(p.onBoard) contents(p.x)(p.y) else None
  def apply(p: Position) = get(p).get
  def apply(x: Int, y: Int) = get(Position(x,y))
  def del(p: Position) = Pieces(contents.updated(p.x, contents(p.x).updated(p.y, None)))
  def add(p: Position, what: Piece) = Pieces(contents.updated(p.x, contents(p.x).updated(p.y, Some(what))))
  def contains(p: Position) = get(p).nonEmpty
  def find(p: Piece): List[Position] = 
    for{
      x <- List.range(0,8)
      y <- List.range(0,8)
    if apply(x,y) == Some(p) } yield Position(x,y)
}
