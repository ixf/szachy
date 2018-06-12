package brt.szachy.shared.logic

sealed trait Color { 
  val name: String
  val abbrev: Char
  val other: Color
}
case object White extends Color {
  val name = "white"
  val abbrev = 'l'
  val other = Black
}
case object Black extends Color {
  val name = "black"
  val abbrev = 'd'
  val other = White
}

case object Angry extends Color {
  val name = "angry"
  val abbrev = 'a'
  val other = White
}

  
