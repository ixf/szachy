package brt.szachy.shared.logic

import brt.szachy.shared.Move

object Board {
  val moved_default: Vector[Vector[Boolean]] = Vector.tabulate(8,8){ (i,j) => false }
  def apply(pieces: Pieces) = new Board(pieces,White, moved_default, Move(Position(-1,-1), Position(-1,-1)))
  def apply() = new Board(Pieces(),White, moved_default, Move(Position(-1,-1),Position(-1,-1)))
}

case class Board(pieces: Pieces, turn: Color, moved: Vector[Vector[Boolean]], last_move: Move) {
  // moved do roszadach
  // last_move do bicia w przelocie
  
  def valid(move: Move): Boolean = validAs(move, turn)

  def validAs(move: Move, as: Color): Boolean = {
    val p_a = pieces.get(move.from)
    val p_b = pieces.get(move.to)
    val enemy = as.other

    p_a match {
      case None => false
      case Some(Piece(`enemy`,_)) => false
      case Some(a) => 
        a.canMove(move.from, move.to, this) &&
        isCheck(move.from, move.to, turn)
    }
  }

  def move(m: Move): Board = move(m.from, m.to)
  def move(from: Position, to: Position): Board = {
    val p_a = pieces(from)

    val new_moved = moved.updated(from.x, moved(from.x).updated(from.y, false))
      .updated(to.x, moved(to.x).updated(to.y, false))

    if ( p_a.canEnPass(from, to, this).nonEmpty){
      Board(pieces.del(from).del(p_a.canEnPass(from, to, this).get).add(to, p_a),
        turn.other, new_moved, Move(from,to))
    } else if ( p_a.canCastle(from, to, this).nonEmpty ){
      val (rook_pos, target) = p_a.canCastle(from, to, this).get
      val rook = pieces(rook_pos)
      Board(pieces.del(from).del(rook_pos).add(to, p_a).add(target, rook),
        turn.other, new_moved, Move(from,to))
    } else {
      val resulting_piece = p_a match {
        case Piece(White,Pawn) if (to.y == 0) =>
            Piece(White,Queen)
        case Piece(Black,Pawn) if (to.y == 7) =>
            Piece(Black,Queen)
        case Piece(k,f) => 
            Piece(k,f)
        case x => x
      }

      Board(pieces.del(from).add(to, resulting_piece), turn.other, new_moved, Move(from,to))
    }
  }

  def remove(from: Position): Board = Board(pieces.del(from), turn, moved, last_move)

  def checkmate(checked: Color): Boolean = {
    // czy gracz checked został zaszachmatowany
    // dla każdej figury jego koloru, dla każdego miejsca gdzie dana figura może pójść
    
    for( x <- pieces.contents.indices; y <- pieces.contents(x).indices )
      if( pieces.contents(x)(y).nonEmpty ){
        val pos = Position(x,y)
        val p = pieces.contents(x)(y).get
        if( p.color == checked )
          for( spot <- p.reachables(pos, this) )
            if( isCheck(pos, spot, checked) ){
              return false;
            }
      }
    true
  }

  def isCheck(from: Position, to: Position, as: Color): Boolean = {
    // czy król nie będzie szachowany po wykonaniu ruchu

    // znajdz krola danego koloru, zobacz czy ktos mu zagra
    // idziemy w kierunkach Hetmana i Skoczka
    // czy kandydat faktycznie szachuje krola

    val tempBoard = move(from, to) // wersja po wykonaniu ruchu
    val kingPos = tempBoard.pieces.find(Piece(as,King)).head // znajdź mojego króla
    val danger_spots = List(Piece(as, Queen), Piece(as,Knight)).flatMap( f => { f.reachables(kingPos, tempBoard) } )
    
    ! ( danger_spots.map( (pos: Position) => (pos, tempBoard.pieces.get(pos)))
      .exists{
        case (pos: Position, Some(p@Piece(color,_))) if color == as.other =>
          p.canMove(pos, kingPos, tempBoard)
        case x => 
          false
      } )
  }
}
