package uec.teehardware.uecutils

import firrtl._
import firrtl.ir._
import firrtl.annotations._
import firrtl.stage.FirrtlCircuitAnnotation
import firrtl.passes.Pass

import java.io.File
import firrtl.annotations.AnnotationYamlProtocol._
import firrtl.passes.memlib.ReplSeqMemAnnotation
import firrtl.transforms.BlackBoxResourceFileNameAnno
import net.jcazevedo.moultingyaml._
import com.typesafe.scalalogging.LazyLogging

trait HasMultiTopOptions { self: ExecutionOptionsManager with HasFirrtlOptions =>
  var multiTopOptions = MultiTopOptions()

  parser.note("multi top options")

  parser.opt[Seq[String]]("syn-tops")
    .abbr("mtst")
    .valueName("<syn-top1>,<syn-top2>...")
    .foreach { x =>
      multiTopOptions = multiTopOptions.copy(
        synTops = x
      )
    }.text {
    "use this to set all the synTops <syn-top1,syn-top2,...>"
  }

  parser.opt[String]("harness-top")
    .abbr("mtht")
    .valueName("<harness-top>")
    .foreach { x =>
      multiTopOptions = multiTopOptions.copy(
        harnessTop = Some(x)
      )
    }.text {
    "use this to set harnessTop"
  }

  parser.opt[String]("chip-top")
    .abbr("mtct")
    .valueName("<chip-top>")
    .foreach { x =>
      multiTopOptions = multiTopOptions.copy(
        chipTop = Some(x)
      )
    }.text {
    "use this to set chipTop"
  }

}

case class MultiTopOptions(
   synTops: Seq[String] = Seq(),
   harnessTop: Option[String] = None,
   chipTop: Option[String] = None
 ) extends LazyLogging