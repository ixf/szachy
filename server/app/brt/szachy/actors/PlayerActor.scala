package brt.szachy.actors

import akka.actor._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import brt.szachy.shared._
import brt.szachy.shared.logic._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

object PlayerActor {
  def props(rng: scala.util.Random, out: ActorRef) = Props(new PlayerActor(rng, out))
}

class PlayerActor(rng: scala.util.Random, out: ActorRef) extends Actor {
  def receive = { case m: String => 
      println(m)
      val y = decode[SharedMessages](m)
      y match {
        case Right(Newgame()) =>
          val r = rng.alphanumeric.filter(_.isLower).take(5).mkString
          println(r)
          out ! r
        case Right(Join(id)) =>
          implicit val timeout = Timeout(1 seconds)

          context.system.actorSelection("user/*/*/"+id).resolveOne().onComplete {
            case Success(actorRef) => 
              actorRef ! self
              context.become(get_color(actorRef))
            case Failure(ex) =>
              val p = context.actorOf(GameActor.props(self), id)
              context.become(get_color(p))
          }
        case x => println(s"Nie wiem co to: $x")
      }
  }

  def get_color(arbiter: ActorRef): Receive = {
    case msg@JoinOK(Some(color)) =>
      out ! msg.asInstanceOf[SharedMessages].asJson.noSpaces
      context.become(game(color,arbiter))
    case msg@JoinOK(None) =>
      out ! msg.asInstanceOf[SharedMessages].asJson.noSpaces
      context.become(spectate(arbiter))
  }


  def spectate(artiber: ActorRef): Receive = {
    case x: SharedMessages =>
      println(x)
      out ! x.asJson.noSpaces
  }


  def game(color: Color, arbiter: ActorRef): Receive = {
    case m: String => 
      val y = decode[SharedMessages](m)
      y match {
        case Right(Move(from, to)) =>
          arbiter ! (color, Move(from, to))
      }
    case x: SharedMessages =>
      println(x)
      out ! x.asJson.noSpaces
  }
}
