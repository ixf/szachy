

package brt.szachy.shared.logic

sealed trait Action {

}

case class Movement() extends Action {
}

case class Take() extends Action {
}

case class Castle() extends Action {
}

case class Promote() extends Action {
}

case class Enp() extends Action {
}

