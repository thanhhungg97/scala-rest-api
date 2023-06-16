package scala.rest.api

import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object HttpLowLevel {

	def main(args: Array[String]): Unit = {
		val logger = Logger.apply(HttpLowLevel.getClass, HttpLowLevel.getClass.getSimpleName)
		implicit val system = ActorSystem()
		implicit val executionContext = system.dispatcher
		val requestHandler: HttpRequest => HttpResponse = {
			case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
				logger.info("Receive request")
				Thread.sleep(10000)
				HttpResponse(entity = HttpEntity(
					ContentTypes.`text/html(UTF-8)`,
					"<html><body>Hello world!</body></html>"))
			}


			case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
				HttpResponse(entity = "PONG!")

			case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
				sys.error("BOOM!")

			case r: HttpRequest =>
				r.discardEntityBytes() // important to drain incoming HTTP Entity stream
				HttpResponse(404, entity = "Unknown resource!")
		}


		val bindFuture: Future[Http.ServerBinding] = Http().newServerAt("localhost", 8080).bindSync(requestHandler)
		bindFuture.onComplete {
			case Success(value) => logger.info("Binding successfully {}", value);
			case Failure(exception) => logger.info("Binding fail", exception)
		}
	}


}
