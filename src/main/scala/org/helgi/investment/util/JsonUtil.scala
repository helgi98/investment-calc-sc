package org.helgi.investment.util

import cats.effect.kernel.Concurrent
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{CirceInstances, jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

object JsonUtil {

  import CirceInstances.*

  implicit def entityEncoder[F[_], A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  implicit def entityDecoder[F[_] : Concurrent, A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
}
