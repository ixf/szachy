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
    {
      case who: ActorRef =>
        white ! Join("You're playing as white")
        who ! Join("You're playing as black")
        context.become(game(board,white,who, List()))
    }
  }

  def game(board: Board, white: ActorRef, black: ActorRef, observers: List[ActorRef]): Receive = {
    case someone: ActorRef =>
      someone ! Join("You're spectating")
      val r = Refresh(board.pieces)
      context.become(game(board,white,black, observers:+ someone))
    case (color, move@Move(from, to)) if color == board.turn =>
       if ( board.valid(move) ) {
         val newBoard = board.move(move)
         white ! move
         black ! move
         for( a <- observers ) { a ! move }
         context.become(game(newBoard,white,black,observers))
       }
  }

}
