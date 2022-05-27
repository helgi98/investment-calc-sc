package org.helgi.investment.util

import io.circe.Encoder
import org.http4s.EntityEncoder
import org.http4s.circe.{CirceInstances, jsonEncoderOf}

object JsonUtil {

  import CirceInstances.*

  implicit def entityEncoder[F[_], A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
}
