package brt.szachy

import brt.szachy.shared._
import brt.szachy.shared.logic._
import org.scalajs.dom
import org.scalajs.dom.WebSocket
import org.scalajs.dom.raw.HTMLButtonElement
import org.scalajs.dom.raw.HTMLLinkElement

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

object NewGame {

  def main(args: Array[String]): Unit = {

    val button = dom.document.getElementById("new_game").asInstanceOf[HTMLButtonElement]
    val result = dom.document.getElementById("link_dest").asInstanceOf[HTMLLinkElement]
    button.onclick = { (e: dom.MouseEvent) => 
      val hostname = dom.window.location.hostname + ":9000"
      val to = "ws://" + hostname + "/ws"
      println(to)
        //"ws://localhost:9000/ws"
      val s = new WebSocket(to)
      s.onmessage = {
        (e: dom.MessageEvent) => {
          println(e.data)
          result.textContent = hostname + "/game/" + e.data.toString
          result.href = "http://" + result.textContent
          s.close()
        }
      }
      s.onopen = { (e: dom.Event) =>
        val x: SharedMessages = Newgame()
        s.send(x.asJson.noSpaces)
      }
    }
  }
}
