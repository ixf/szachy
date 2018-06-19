
import org.specs2._
import brt.szachy.shared.logic._
import brt.szachy.shared.Move

class BoardSpec extends org.specs2.mutable.Specification {

  val all_moved: Vector[Vector[Boolean]] = Vector.tabulate(8,8){ (i,j) => false }
  val empty = Pieces(Vector.tabulate(8,8){ (i,j) => (None: Option[Piece]) })
  val board1 = Board( empty
    .add(Position(1,1), Piece(Black, Queen) )
    .add(Position(4,6), Piece(Black, King) )
    .add(Position(6,7), Piece(White, King) ),
    White, all_moved, Move(Position(-1,-1), Position(-1,-1)))

  val board2 = Board( empty
    .add(Position(1,3), Piece(White, Pawn))
    .add(Position(0,1), Piece(Black, Pawn))
    .add(Position(2,1), Piece(Black, Pawn))
    .add(Position(7,7), Piece(White, Rook))
    .add(Position(4,7), Piece(White, King))
    )

  val board3 = Board( board1.pieces.add( Position(3,3), Piece(Black, Rook) ) )
  val board4 = Board( board1.pieces.add( Position(6,6), Piece(Black, Queen) ) )

  "King" should {
    "be in check" in {
      board1.isCheck(Position(6,7), Position(6,6), White)
      board1.isCheck(Position(6,7), Position(5,7), White)
      board1.isCheck(Position(4,6), Position(3,6), Black)
    }
    "not be in check" in {
      board3.isCheck(Position(6,7), Position(6,6), White)
    }
    "be under checkmate" in {
      board4.checkmate(White)
    }
    "castle" in {
      board2.valid(Move(Position(4,7), Position(6,7)))
      board2.move(Move(Position(4,7), Position(6,7)))
        .pieces.contents(5)(7) == Some(Piece(White,Rook))
    }
    "not castle" in {
      ! board2.valid(Move(Position(4,7), Position(7,7)))
      ! board2.valid(Move(Position(4,7), Position(2,7)))
    }
  }

  "Rook" should {
    "move" in {
      board2.valid(Move(Position(7,7), Position(7,0)))
      board2.valid(Move(Position(7,7), Position(7,6)))
    }
    "be blocked" in {
      ! board2.valid(Move(Position(7,7), Position(3,7))) // za królem
      ! board2.valid(Move(Position(7,7), Position(4,7))) // bicie własnego króla
      ! board2.valid(Move(Position(7,7), Position(4,4))) // na skos
    }
  }

  "White Pawn" should {
    "move forward" in {
      Piece(White,Pawn).canMove(Position(1,3), Position(1,2), board2)
    }
    "not jump fowrard" in {
      ! Piece(White,Pawn).canMove(Position(1,3), Position(1,1), board2)
    }
    "capture en passant" in {
      Piece(White,Pawn).canMove(Position(1,3), Position(0,2), board2.move(Move(Position(0,1), Position(0,3))))
    }
    "not capte en passant" in {
      ! Piece(White,Pawn).canMove(Position(1,3), Position(0,2), board2.move(Move(Position(0,1), Position(0,3))).move(Move(Position(2,1),Position(2,3))))
    }
    "get promoted" in {
      board2.move(Position(1,3), Position(1,2))
        .move(Position(1,2), Position(1,1))
        .move(Position(1,1), Position(1,0))
        .pieces.contents(1)(0) == Some(Piece(White, Queen))
    }
  }

  "Black Pawns" should {
    "move forward" in {
      Piece(Black,Pawn).canMove(Position(2,1), Position(2,2), board2)
      Piece(Black,Pawn).canMove(Position(0,1), Position(0,2), board2)
    }
    "jump forward" in {
      Piece(Black,Pawn).canMove(Position(2,1), Position(2,3), board2)
      Piece(Black,Pawn).canMove(Position(0,1), Position(0,3), board2)
    }
  }



}
