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

object BufferedPost extends App {

  val body = "Hola".getBytes(StandardCharsets.UTF_8)
  val program = for {
    queue <- Queue.unbounded[Byte]
    _ <- loop(body, queue).fork
    stream = Stream.fromQueue(queue)
    data = Request.Data(List(Header("Host", "httpbin.org")), HttpData.StreamData(stream))
    url <- ZIO.fromEither(URL.fromString("http://httpbin.org/post"))
    req = Request((Method.POST, url), data)
    res <- Client.request(req)
    _ <- queue.awaitShutdown
    _ <- console.putStrLn(res.content match {
      case CompleteData(data) => data.map(_.toChar).mkString
      case Empty => ""
      case StreamData(data) => "<Chunk>"
    })
  } yield ()

  def loop(bytes: Array[Byte], queue: Queue[Byte]): UIO[Unit] = {
    if (bytes.size == 0) {
      queue.shutdown
    } else {
      queue.offer(bytes.head)
      loop(bytes.tail, queue)
    }
  }

  val env = ChannelFactory.auto ++ EventLoopGroup.auto()
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode.provideCustomLayer(env)

}
