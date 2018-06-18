package brt.szachy.shared.logic

import scala.annotation.tailrec

sealed trait Figure {
  val name: String
  val dirs: List[Direction] 
  lazy val abbrev: Char = name(0).toLower
  def reachables(from: Position, board: Board, color: Color): List[Position]
  def canMove(form: Position, to: Position, board: Board, color: Color): Boolean
}


sealed trait RepeatedMoves extends Figure {
  override def canMove(from: Position, to: Position, board: Board, color: Color): Boolean = {
    this.reachables(from, board, color).contains(to)
  } // TODO wolne

  def reachables(from: Position, board: Board, color: Color): List[Position] = {

    def gen_dir(dir: Direction, from: Position): List[Position] = {
      if ( from(dir).onBoard ) {
        if ( board.pieces.get(from(dir)).isEmpty )
          List(from(dir)) ++ gen_dir(dir,from(dir))
        else if ( board.pieces(from(dir)).color != color )
          List(from(dir))
        else
          List()
      } else 
        List()
    }

    ( for( (d: Direction) <- this.dirs ) yield gen_dir(d, from) ).flatten
  }
}

sealed trait SimpleMoves extends Figure {
  override def canMove(from: Position, to: Position, board: Board, color: Color): Boolean = {
    this.reachables(from, board, color).contains(to)
  }
  def reachables(from: Position, board: Board, color: Color): List[Position] = {
    dirs.filter( d => ( board.pieces.get(from(d)).isEmpty || board.pieces(from(d)).color != color ) && from(d).onBoard )
      .map( d => from(d) )
  }
}

case object King extends SimpleMoves {
  val name = "King"
  val dirs: List[Direction] = Queen.dirs
}

case object Knight extends SimpleMoves {
  val name = "Knight"
  override lazy val abbrev = 'n'
  val dirs: List[Direction] = List(
    up(right)(right), up(left)(left), down(right)(right), down(left)(left),
    up(up)(right), up(up)(left), down(down)(right), down(down)(left)
  )

}

case object Pawn extends Figure {
  val name = "Pawn"

  val dirs = List(up(up), down(down), up, down, up(left), up(right), down(left), down(right))

  def reachables(from: Position, board: Board, color: Color): List[Position] =
    for( d <- dirs if canMove(from, from(d), board, color )) yield from(d)

  override def canMove(from: Position, to: Position, board: Board, color: Color): Boolean = {
    val next = if ( color == White ) from(up) else from(down)
    if ( next.onBoard ) 
      if ( next == to ){
        board.pieces.get(to).isEmpty
      } else if ( next(left) == to || next(right) == to ) {
        board.pieces.get(to).nonEmpty && board.pieces(to).color != color
      } else if ( to == next(up) && color == White && from.y == 6 ){
        board.pieces.get(next).isEmpty && board.pieces.get(next(up)).isEmpty
      } else if ( to == next(down) && color == Black && from.y == 1 ){
        board.pieces.get(next).isEmpty && board.pieces.get(next(down)).isEmpty
      } else 
        false 
    else
      false
  }
}

case object Queen extends RepeatedMoves {
  val name = "Queen"
  val dirs: List[Direction] = Rook.dirs ++ Bishop.dirs
}

case object Rook extends RepeatedMoves {
  val name = "Rook"
  val dirs: List[Direction] = List(up, down, left, right)
}

case object Bishop extends RepeatedMoves {
  val name = "Bishop"
  val dirs: List[Direction] = List(up(left), up(right), down(left), down(right))
}
