package brt.szachy.shared.logic

import scala.annotation.tailrec

sealed trait Figure {
  val name: String
  val dirs: List[Direction] 
  lazy val abbrev: Char = name(0).toLower
  def reachables(from: Position, board: Board, color: Color): List[Position]
  def canMove(form: Position, to: Position, board: Board, color: Color): Boolean

  def canCastle(from: Position, to: Position, board: Board, color: Color): Option[(Position,Position)] = None
}


sealed trait RepeatedMoves extends Figure {
  override def canMove(from: Position, to: Position, board: Board, color: Color): Boolean = {
    this.reachables(from, board, color).contains(to)
  }

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


case object King extends Figure{
  val name = "King"
  val dirs: List[Direction] = Queen.dirs ++ List(left(left), right(right))

  def reachables(from: Position, board: Board, color: Color): List[Position] =
    for( d <- dirs if canMove(from, from(d), board, color )) yield from(d)

  def canMove(from: Position, to: Position, board: Board, color: Color): Boolean = {
    if( from.distance( to ) == 1 || from.absDelta( to ) == (1,1) )
      ( ( board.pieces.get(to).isEmpty || board.pieces(to).color != color ) && to.onBoard )
    else
      canCastle(from, to, board, color).nonEmpty
  }

  override def canCastle(from: Position, to: Position, board: Board, color: Color): Option[(Position, Position)] = {
    if( from.absDelta(to) == (2,0) && ! board.moved(from.x)(from.y) && to.x != from.x ){

      // nie może być żadnej figury między królem a wieżą + wieża nieruszona
      val (leftSide, rightSide, rook_x, rook_dir) = if( to.x < from.x ){
        (0, from.x, 0, left)
      } else {
        (from.x, 7, 7, right)
      }

      if( ( ! board.pieces.contents.slice(leftSide+1, rightSide).map( v=>v(from.y)).exists( _ != None ) ) &&
          ( ! board.moved(rook_x)(from.y) ) && board.pieces.get(Position(rook_x,from.y)).orElse(Some(Piece(White,Pawn))).get.figure == Rook){
        Some(Position(rook_x, from.y), from(rook_dir))
      } else {
        None
      }
    } else{
      None
    }
  }
}

case object Knight extends Figure{
  val name = "Knight"
  override lazy val abbrev = 'n'
  val dirs: List[Direction] = List(
    up(right)(right), up(left)(left), down(right)(right), down(left)(left),
    up(up)(right), up(up)(left), down(down)(right), down(down)(left)
  )

  override def canMove(from: Position, to: Position, board: Board, color: Color): Boolean = {
    this.reachables(from, board, color).contains(to)
  }

  def reachables(from: Position, board: Board, color: Color): List[Position] = {
    dirs.filter( d => ( board.pieces.get(from(d)).isEmpty || board.pieces(from(d)).color != color ) && from(d).onBoard )
      .map( d => from(d) )
  }
}

case object Pawn extends Figure {
  val name = "Pawn"

  val dirs = List(up(up), down(down), up, down, up(left), up(right), down(left), down(right))

  def reachables(from: Position, board: Board, color: Color): List[Position] =
    for( d <- dirs if canMove(from, from(d), board, color )) yield from(d)

  def canEnPass(from: Position, to: Position, board: Board, color: Color): Option[Position] = {
    val dir = if(color == White) up else down
    val next = from(dir)
    val m = board.last_move

    if ( next.onBoard && ( next(left) == to || next(right) == to ) ) {
      val a = board.pieces.get(to(dir.negate())) == Some(Piece(color.other, Pawn))
      val b = m.from == to(dir) && m.to == to(dir.negate())
      if ( a && b )
        Some( to(dir.negate()) )
      else
        None
    } else
      None
  }

  override def canMove(from: Position, to: Position, board: Board, color: Color): Boolean = {
    val dir = if(color == White) up else down
    val next = from(dir)
    if ( next.onBoard )
      if ( next == to ){ // ruch do przodu
        board.pieces.get(to).isEmpty
      } else if ( next(left) == to || next(right) == to ) { // bicie
        ( board.pieces.get(to).nonEmpty && board.pieces(to).color != color ) ||
        canEnPass(from, to, board, color).nonEmpty
      } else if ( to == next(up) && color == White && from.y == 6 ){ // ruch o 2
        board.pieces.get(next).isEmpty && board.pieces.get(next(up)).isEmpty
      } else if ( to == next(down) && color == Black && from.y == 1 ){ // ruch o 2 z drugiej strony
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
