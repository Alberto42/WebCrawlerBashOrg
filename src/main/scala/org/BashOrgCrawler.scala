package org

import java.io.{File, PrintWriter}

import org.json.JSONObject
import org.jsoup.HttpStatusException

import scala.jdk.CollectionConverters._
import com.typesafe.config._

/*
Fetch n newest entries from bash.org.pl and parse id, points and contents.
Then save it as a jsons into specified file.
 */
object BashOrgCrawler {

  private val outputPath = {
    ConfigFactory
      .load("application.conf")
      .getString("outputPathRelative")
  }

  def main(args: Array[String]): Unit = {
    if (args.size != 1) {
      System.err.println("Usage: BashOrgCrawler <expectedDialogsCount>")
      System.exit(1)
    }
    val expectedDialogsCount = args(0).toInt
    val output = try {
      crawl(expectedDialogsCount)
    } catch {
      case e: HttpStatusException =>
        throw new IllegalStateException("Cannot fetch dialogs. Probably you've asked for too many of them.", e)
    }

    var writer: PrintWriter = null
    try {
      writer = new PrintWriter(new File(outputPath))
      writer.write(output)
    }
    finally {
      writer.close()
    }
  }

  private def crawl(expectedDialogsCount: Int) = {
    (1 to expectedDialogsCount).map(_ => {
      val dialog = DialogsFetcher.getNextDialog()

      val id = dialog.attr("id").substring(1)

      val points = {
        val elements = dialog
          .getElementsByAttributeValue("class", " points")
          .asScala
          .flatMap(dialog => dialog.childNodes().asScala)
          .toList
        assert(elements.length == 1)
        elements(0).toString.toInt
      }

      val content = dialog
        .child(1).childNodes()
        .asScala.toList
        .map(_.toString)
        .reduce((a, b) => a + b)
        .replaceAllLiterally("&gt;", ">")
        .replaceAllLiterally("&lt;", "<")

      new JSONObject()
        .put("id", id)
        .put("points", points.toString)
        .put("content", content)
        .toString(2)

    }).reduce((a, b) => a + "\n" + b)
  }
}
