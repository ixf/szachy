package brt.szachy.shared.logic

import brt.szachy.shared.Move

object Board {
  def apply(pieces: Pieces) = new Board(pieces,White)
  def apply() = new Board(Pieces(),White)
}

case class Board(pieces: Pieces,turn: Color) {

  // val unmovedCastlers
  // val unmoved

  def valid(move: Move): Boolean = {
    val p_a = pieces.get(move.from)
    val p_b = pieces.get(move.to)
    val enemy = turn.other

    p_a match {
      case None => false
      case Some(Piece(`enemy`,_)) => false
      case Some(a) => 
        println(a.canMove(move.from, move.to, this))
        println(( ! isLegal(move.from, move.to, turn) ))
        a.canMove(move.from, move.to, this) &&
        ( ! isLegal(move.from, move.to, turn) )
    }
  }

  def move(m: Move): Board = move(m.from, m.to)
  def move(from: Position, to: Position): Board = {
    val p_a = pieces.get(from)
    val p_b = pieces.get(to)
    Board(pieces.del(from).add(to, pieces(from)), turn.other)
  }

  def remove(from: Position): Board = Board(pieces.del(from), turn)

  def check(board: Board): Boolean = {
    false
  }

  def isLegal(from: Position, to: Position, as: Color): Boolean = {
    // czy król nie będzie szachowany

    // znajdz krola danego koloru, zobacz czy ktos mu zagra
    // idziemy w kierunkach Hetmana i Skoczka, a nastepnie sprawdzamy czy
    // kandydat faktycznie szachuje krola

    val kingPos = pieces.find(Piece(as,King)).head
    val tempBoard = move(from, to) // niekoniecznie poprawna
    val providers = List(Piece(as, Queen), Piece(as,Knight))
    val source = if( from == kingPos ) to else kingPos

    providers.exists( fig =>
        fig.reachables(source, this)
          .map( (pos: Position) => { (pos, tempBoard.pieces.get(pos)) })
          .exists{
            case (pos, Some(p@Piece(color,_))) if color == as.other =>
              val r = p.canMove(pos, source, tempBoard)
              println((r,pos,kingPos,p))
              r
            case x => 
              println(("false",x))
              false
          }
        )
  }
}
