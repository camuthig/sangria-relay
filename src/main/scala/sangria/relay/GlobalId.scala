package sangria.relay

import sangria.relay.util.Base64
import sangria.schema.{IDType, ScalarAlias}
import sangria.validation.{ValueCoercionViolation, Violation}

case class GlobalId(typeName: String, id: String) {
  def asString = GlobalId.toGlobalId(this)
}

object GlobalId {
  def toGlobalId(globalId: GlobalId): String = toGlobalId(globalId.typeName, globalId.id)

  /**
   * Takes a type name and an ID specific to that type name, and returns a
   * "global ID" that is unique among all types.
   */
  def toGlobalId(typeName: String, id: String): String = Base64.encode(s"$typeName:$id")

  /**
   * Takes the "global ID" created by toGlobalID, and retuns the type name and ID
   * used to create it.
   */
  def fromGlobalId(globalId: String) = {
    val decoded = Base64.decode(globalId)
    val idx = decoded.indexOf(":")

    if (idx < 0 && (decoded.size - 1) != idx)
      None
    else
      Some(GlobalId(decoded.substring(0, idx), decoded.substring(idx + 1)))
  }

  def unapply(globalId: String) = fromGlobalId(globalId)

  case object IdViolation extends ValueCoercionViolation("Invalid ID")

  /**
   * Creates a type alias to support using GlobalIDs across Relay applications
   */
  implicit val GlobalIdTypeAlias: ScalarAlias[GlobalId, String] = ScalarAlias[GlobalId, String](IDType,
    toScalar = _.asString,
    fromScalar = id ⇒ GlobalId.fromGlobalId(id).fold[Either[Violation, GlobalId]](Left(IdViolation))(Right(_)))
}
