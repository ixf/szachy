package brt.szachy.shared.logic

case class Piece(color: Color, figure: Figure){
  def abbrev = "" + figure.abbrev + color.abbrev
  def picture: String = "Chess_" + abbrev + "t45.png"

  def canMove(from: Position, to: Position, board: Board): Boolean = {
    figure.canMove(from, to, board, color)
  }

  def reachables(from: Position, board: Board): List[Position] =
    figure.reachables(from, board, color)

}

