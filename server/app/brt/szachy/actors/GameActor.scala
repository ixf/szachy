package brt.szachy.actors

import akka.actor._
import brt.szachy.shared.logic._
import brt.szachy.shared._

object GameActor {
  def props(out: ActorRef) = Props(new GameActor(out))

  case class JoinGame(who: ActorRef){}
}

class GameActor(out: ActorRef) extends Actor {

  def receive = join(Board(), out)

  def join(board: Board, white: ActorRef): Receive = {
    println(("Test: ", Piece(Black, Queen).reachables(Position(7,6), board)));

    {
      case who: ActorRef =>
        white ! JoinOK(Some(White))
        who ! JoinOK(Some(Black))
        context.become(game(board,white,who, List()))
    }
  }

  def game(board: Board, white: ActorRef, black: ActorRef, observers: List[ActorRef]): Receive = {

    case someone: ActorRef =>
      someone ! JoinOK(None)
      someone ! Refresh(board.pieces)
      context.become(game(board,white,black, observers:+ someone))

    case (color, move@Move(from, to)) if color == board.turn =>
      println("got a move from ", color)
      println(move)
      if ( board.valid(move) ) {
        println("move valid")
        val newBoard = board.move(move)
        white ! move
        black ! move
        for( a <- observers ) { a ! move }
        if( newBoard.checkmate(newBoard.turn) ){
          println("checkmate");
          white ! Checkmate(newBoard.turn.other)
          black ! Checkmate(newBoard.turn.other)
          for( a <- observers ) { a ! Checkmate(newBoard.turn.other) }
        }
        context.become(game(newBoard,white,black,observers))
      } else {
        println("move not valid")
      }
  }

}
