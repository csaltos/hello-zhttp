import zhttp.http.{Endpoint, HttpData, Request, URL, Method, Header}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio._
import zio.stream._
import java.nio.file.Path
import java.util.ArrayList
import java.nio.charset.StandardCharsets
import zhttp.http.HttpData.CompleteData
import zhttp.http.HttpData.Empty
import zhttp.http.HttpData.StreamData

object SimpleGet extends App {
  val env = ChannelFactory.auto ++ EventLoopGroup.auto()
  val program = for {
    url <- ZIO.fromEither(URL.fromString("http://httpbin.org/get"))
    data = Request.Data(List(Header("Host", "httpbin.org")), HttpData.empty)
    req = Request((Method.GET, url), data)
    res <- Client.request(req)
    _ <- console.putStrLn(res.content match {
      case CompleteData(data) => data.map(_.toChar).mkString
      case Empty => ""
      case StreamData(data) => "<Chunk>"
    })
  } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode.provideCustomLayer(env)

}