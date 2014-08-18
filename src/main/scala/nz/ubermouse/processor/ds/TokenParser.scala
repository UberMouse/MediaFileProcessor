package nz.ubermouse.processor.ds

import scala.util.matching.Regex
import java.util.regex.Matcher
import reflect.runtime.universe._
import reflect.runtime.currentMirror

sealed abstract class Token
case class ReleaseGroup(name: String) extends Token
case class Title(title: String) extends Token
case class Episode(episode: Int) extends Token
case class Season(season: Int) extends Token
case class CatchAll(chunk: String) extends Token
case class CRC(crc: String) extends Token

case class ParseResult(remainder: String, token: Token)

case class ParseNode(token: Token, children: Iterable[ParseNode]) {
  def contains(token: Token): Boolean = children.exists(_.token.getClass == token.getClass) || children.exists(node => node.contains(token))
  override def toString = s"ParseNode($token, ${children.size})"
}

abstract class TokenConsumer {
  def matcher(title: String): Regex
  def consumer(matched: Matcher): Token

  def parse(name: String, title: String): Option[ParseResult] = {
    val m = matcher(title)
    val matched = m.pattern.matcher(name)

    if(matched.find() && valid(matched)) {
      Option(ParseResult(name.drop(matched.group().length), consumer(matched)))
    }
    else None
  }

  def valid(matched: Matcher) = true
}

class ReleaseGroupConsumer extends TokenConsumer {
  def matcher(title: String) = """^\[(.+?)\]""".r
  def consumer(matched: Matcher): ReleaseGroup = ReleaseGroup(matched.group(1))

  override def valid(matched: Matcher) = {
    val m = matched.group(1)
    m != "720p" && m != "1080p"
  }
}

class TitleConsumer extends TokenConsumer {
  def matcher(title: String) = s"^($title)".r
  def consumer(matched: Matcher): Title = Title(matched.group(1))
}

class EpisodeConsumer extends TokenConsumer {
  def matcher(title: String) = """^(?i:E?P? ?([0-9]{1,4})) """.r
  def consumer(matched: Matcher): Episode = {
    val maybeNum = matched.group(1).dropWhile(_ == '0')
    val num = {
      if(maybeNum.length > 0) maybeNum.toInt
      else -1
    }
    Episode(num)
  }
}

class SeasonConsumer extends TokenConsumer {
  def matcher(title: String) = """^(?i:S?([0-9]{1,2})) """.r
  def consumer(matched: Matcher): Season = {
    val maybeNum = matched.group(1).dropWhile(_ == '0')
    val num = {
      if(maybeNum.length > 0) maybeNum.toInt
      else -1
    }
    Season(num)
  }
}

class CatchAllConsumer extends TokenConsumer {
  def matcher(title: String) = "^.".r
  def consumer(matched: Matcher): CatchAll = CatchAll(matched.group())
}

class CRCConsumer extends TokenConsumer {
  def matcher(title: String) = """^\[([A-Z0-9]+?)\]""".r
  def consumer(matched: Matcher): CRC = CRC(matched.group(1))

  override def valid(matched: Matcher) = {
    val crc = matched.group(1)
    crc.matches(".*[0-9].*") && crc.matches(".*[A-Z].*")
  }
}

class TokenParser extends NameParser {

  val consumers = List[TokenConsumer](
    new CRCConsumer,
    new ReleaseGroupConsumer,
    new TitleConsumer,
    new EpisodeConsumer,
    new SeasonConsumer,
    new CatchAllConsumer
  )

  def tokenize(name: String, title: String) = {
    def tokenizeWithTitle(name: String, title: String): List[ParseNode] = {
      val results = consumers.flatMap(p => p.parse(name, title))
      if(results.length == 1) {
        var n = name
        while(consumers.flatMap(p => p.parse(n, title)).length == 1)
          n = n.drop(1)
        List(ParseNode(CatchAll(name.take(name.length-n.length)), tokenizeWithTitle(n, title)))
      }
      else {
        if(results.length == 0) return List[ParseNode]()
        List(ParseNode(results.head.token, tokenizeWithTitle(results.head.remainder, title)))
      }
    }
    tokenizeWithTitle(name, title)
  }

  def extract(root: ParseNode): Option[ParsedMediaFile] = {
    var title = ""
    var episode = -1
    var releaseGroup = ""
    var season = -1

    def recursive(node: ParseNode) {
      val children = node.children
      children.foreach {
        case n @ ParseNode(Title(t), _) if title == "" => title = t; recursive(n)
        case n @ ParseNode(Episode(e), _) if episode == -1 => episode = e; recursive(n)
        case n @ ParseNode(ReleaseGroup(g), _) if releaseGroup == "" => releaseGroup = g; recursive(n)
        case n @ ParseNode(Season(s), _) if season == -1 => season = s;recursive(n)
        case n => recursive(n)
      }
    }
    recursive(ParseNode(null, List(root)))

    if(title != "" && episode != -1)
      Option(ParsedMediaFile(releaseGroup, title, if(season == -1) 1 else season, episode))
    else
      None
  }

  def parse(name: String, titles: Iterable[String]): Option[ParsedMediaFile] = {
    val tokenized_possibilities = titles.flatMap(title => tokenize(name, title).flatMap(extract))
    tokenized_possibilities.headOption
  }

}
