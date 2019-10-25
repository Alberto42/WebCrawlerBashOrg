package org

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import scala.jdk.CollectionConverters._

object DialogsFetcher {

  private final val bashWebsitePath = "http://www.bash.org.pl/latest?page="

  private var dialogs = fetchDialogsFromPage(1)
  private var nextPage: Int = 2
  private var nextDialog: Int = 0

  def getNextDialog(): Element = {
    if (nextDialog < dialogs.size) {
      val res = dialogs(nextDialog)
      nextDialog += 1
      res
    } else {
      nextDialog = 0
      dialogs = fetchDialogsFromPage(nextPage)
      nextPage += 1
      getNextDialog()
    }
  }

  private def fetchDialogsFromPage(index: Int) = {
    Jsoup
      .connect(bashWebsitePath + index.toString)
      .get()
      .getElementsByAttributeValueMatching("id", "d[0-9]+")
      .asScala
  }
}
