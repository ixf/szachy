package brt.szachy.controllers

import javax.inject._

import brt.szachy.shared.SharedMessages
import brt.szachy.shared._
import play.api.mvc._
import play.api.libs.streams.ActorFlow
import brt.szachy.actors._
import akka.actor.ActorSystem
import akka.stream.Materializer
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

@Singleton
class Application @Inject()(cc: ControllerComponents)
  (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  val rng = scala.util.Random

  def index = Action {
    Ok(views.html.index())
  }

  def game(id: String) = Action {
    Ok(views.html.game("szachy 2k18"))
  }

  def socket = WebSocket.accept[String, String] { request => 
    ActorFlow.actorRef { out =>
      PlayerActor.props(rng, out)
    }
  }

}

