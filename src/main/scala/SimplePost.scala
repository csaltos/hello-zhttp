import zhttp.http.{Endpoint, HttpData, Request, URL, Method, Header}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio._
import zio.stream._
import java.nio.file.Path
import java.util.ArrayList
import java.nio.charset.StandardCharsets
import zio.duration.Duration
import java.util.concurrent.TimeUnit
import zio.clock.Clock
import zhttp.http.HttpData.CompleteData
import zhttp.http.HttpData.Empty
import zhttp.http.HttpData.StreamData

object SimplePost extends App {
  val body = "Hola".getBytes(StandardCharsets.UTF_8)
  val data = Request.Data(List(Header("Host", "httpbin.org")), HttpData.CompleteData(Chunk.fromArray(body)))
  val program = for {
    url <- ZIO.fromEither(URL.fromString("http://httpbin.org/post"))
    req = Request((Method.POST, url), data)
    res <- Client.request(req)
    _ <- console.putStrLn(res.content match {
      case CompleteData(data) => data.map(_.toChar).mkString
      case Empty => ""
      case StreamData(data) => "<Chunk>"
    })
  } yield ()

  val env = ChannelFactory.auto ++ EventLoopGroup.auto()
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode.provideCustomLayer(env)

}