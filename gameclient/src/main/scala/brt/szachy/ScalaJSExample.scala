package brt.szachy

import brt.szachy.shared._
import brt.szachy.shared.logic._
import org.scalajs.dom
import org.scalajs.dom.WebSocket
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

object ScalaJSExample {

  val canvas1 = dom.document.getElementById("gridboard").asInstanceOf[Canvas]
  val ctx1 = canvas1.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  val canvas2 = dom.document.getElementById("chessboard").asInstanceOf[Canvas]
  val ctx2 = canvas2.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  var selected: Option[(Int,Int)] = None
  var board = Board()
  var my_color: Option[Color] = Some(White)

  val img_map: Map[String, HTMLImageElement] = (for( piece <- "kqbrnp"; color <- "ld" ) yield {
    val img = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
    img.src = "/assets/images/Chess_" + piece + color + "t45.png"
    img.onload = (e: dom.Event) => render(ctx2)
    "" + piece + color -> img
  }) toMap

  def render(ctx: dom.CanvasRenderingContext2D) {
    ctx.clearRect(0,0,800,800)
    for( x <- board.pieces.contents.indices )
      for( y <- board.pieces.contents(x).indices )
        board.pieces(x,y) match {
          case Some(v) => ctx.drawImage(img_map(v.abbrev), x*80+80,y*80+80,80,80)
          case None => ()
        }
        selected match {
          case Some((x,y)) => 
            ctx.beginPath()
            ctx.lineWidth=4
            ctx.strokeStyle="red"
            ctx.rect(x*80+80,y*80+80,80,80)
            ctx.stroke()
            println(x,y)

            val piece = board.pieces(Position(x,y))
            val dots = piece.reachables(Position(x,y), board)
            for( p <- dots ){
              if( board.validAs(Move(Position(x,y), p), piece.color)) {
                ctx.beginPath()
                ctx.strokeStyle="rgba(196,196,196,0.8)"
                ctx.arc(p.x*80+120,p.y*80+120, 10, 0, 2*3.1415, false)
                ctx.fillStyle = "rgba(196,196,196,0.8)"
                ctx.fill()
                ctx.stroke()
              }
            }

          case _ => ()
        }
  }

  val hostname = dom.window.location.hostname + ":9000"
  val gameId = dom.window.location.pathname.reverse.takeWhile(_ != '/').reverse
  val to = "ws://" + hostname + "/ws"
  val ws = new WebSocket(to)

  ws.onopen = { (e: dom.Event) =>
    val m: SharedMessages = Join(gameId)
    ws.send(m.asJson.noSpaces)
  }

  ws.onmessage = { (e: dom.MessageEvent) =>
    e.data match { case s: String =>
      println(s)
      val m = decode[SharedMessages](s)
      m match {
        case Right(Refresh(newBoard)) =>
          board = Board(newBoard)
        case Right(Move(from, to)) => 
          println(s"Move $from $to")
          board = board.move(from, to)
          ctx1.clearRect(400,0,800,40)
          ctx1.fillStyle = "rgb(0,0,0)"
          ctx1.textAlign = "right"
          ctx1.fillText(board.turn + "'s turn", 790, 0);
        case Right(JoinOK(color)) =>
          val role_text = color match {
            case None => "You're spectating"
            case Some(c) => "You're playing as " + c.name
          }
          println(role_text)
          my_color = color
          ctx1.fillStyle = "rgb(0,0,0)"
          ctx1.textAlign = "left"
          ctx1.fillText(role_text, 10, 0);
          ctx1.textAlign = "right"
          ctx1.fillText(board.turn + "'s turn", 790, 0);
        case Right(Checkmate(winner)) =>
          ctx1.clearRect(0,0,800,40)
          ctx1.textAlign = "center"
          ctx1.fillText("Checkmate: " + winner.name + " wins.", 400, 0);
        case x => println(x)
      }
    }
    render(ctx2)
  }

  def update(x: Int, y: Int) {
    selected match {
      case Some((`x`,`y`)) => selected = None
      case Some((a,b)) => {
        val m: SharedMessages = shared.Move(Position(a, b), Position(x,y))
        ws.send(m.asJson.noSpaces)
        //board = board.move((Position.apply _).tupled(a), Position(x,y))
        selected = None 
      }
      case None if board.pieces.contains(Position(x,y)) => selected = Some((x,y))
      case None => ()
    }
  }


  def canvas_xy(e: dom.MouseEvent): (Double,Double) = {
    val rect = canvas2.getBoundingClientRect()
    (e.clientX - rect.left, e.clientY - rect.top)
  }

  def clicked_xy(e: dom.MouseEvent): Option[(Int,Int)]= {
    val (x,y) = canvas_xy(e) 
    if( x > 80 && y > 80 && x < 720 && y < 720 ){
      Some((x/80-1).toInt, (y/80-1).toInt)
    } else 
      None
  }

  ctx1.font = "24px Hack"
  ctx1.textAlign = "center"
  ctx1.textBaseline = "top"

  for ( ix <- 1 to 8 ){
    ctx1.fillStyle = "rgb(0,0,0)"
    ctx1.fillText((ix+64).asInstanceOf[Char].toString, ix*80+40, 740);
    ctx1.fillText(ix.toString, 44, 740-ix*80);

    for ( iy <- 1 to 8 ){
      ctx1.fillStyle = if ((ix+iy)%2== 0) "rgb(238,238,210)" else "rgb(118,150,86)";
      ctx1.fillRect(ix*80,iy*80,80,80);
    }
  }

  render(ctx2)

  canvas2.onclick = (e: dom.MouseEvent) => {
    clicked_xy(e) match {
      case None => selected = None
      case Some((x,y)) =>
        update(x,y)
        render(ctx2)
    }
  }


  def main(args: Array[String]): Unit = {
    //dom.window.setInterval(() => gameLoop(ctx2), 1) 
  }
}

