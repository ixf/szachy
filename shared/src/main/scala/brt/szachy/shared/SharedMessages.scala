package brt.szachy.shared

import brt.szachy.shared.logic._

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

sealed trait SharedMessages{}

case class Newgame() extends SharedMessages
case class Join(id: String) extends SharedMessages
case class JoinOK(playing_as: Option[Color]) extends SharedMessages
case class Refresh(board: Pieces) extends SharedMessages
case class Move(from: Position, to: Position) extends SharedMessages
case class Checkmate(winner: Color) extends SharedMessages
