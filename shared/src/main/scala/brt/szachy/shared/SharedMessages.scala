package brt.szachy.shared

import brt.szachy.shared.logic._

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

/*
object SharedMessages{
  def makeList(pm: Pieces): List[(String, Piece)] = 
    pm.contents.collect{ case (k,v) => (k.asJson.noSpaces, v) }.toList
  def makePieces(b: List[(String, Piece)]): Pieces = 
    b.contents.collect( { case (k,v) if decode[Position](k).isRight =>
      decode[Position](k).right.get -> v
    }).toMap
}
*/

sealed trait SharedMessages{
}

case class Newgame() extends SharedMessages
case class Join(id: String) extends SharedMessages
case class Refresh(board: Pieces) extends SharedMessages
case class Move(from: Position, to: Position) extends SharedMessages
case class Error(why: String) extends SharedMessages
