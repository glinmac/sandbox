package ltd.cylleneworks.sandbox.cmusphinx

import java.io.FileInputStream
import java.io.File

import edu.cmu.sphinx.api.Configuration
import edu.cmu.sphinx.api.StreamSpeechRecognizer

import java.io._

import java.util.Properties


object Transcribe {

  case class Config(in: String = "", out: String = "", config: String = "")

  val parser = new scopt.OptionParser[Config]("Test") {
    head("Test", "0.1")

    opt[String]("in").required().valueName("<file>")
      .action((x, c) => c.copy(in=x))
      .text("Input wav file")

    opt[String]("out").required().valueName("<file>")
      .action((x,c) => c.copy(out=x))

      .text("Destination file to store the transcription")

    opt[String]("config").optional().valueName("<file>")
      .action((x,c) => c.copy(config=x))

    help("help").text("Print usage")
  }

  def main(args: Array[String]): Unit = {

    parser.parse(args, Config()) match {

      case None => sys.exit(1)

      case Some(config) => {

        val properties = new Properties()
        properties.load(scala.io.Source.fromFile(config.config).reader())

        val sphynxConfig = new Configuration()
        sphynxConfig.setAcousticModelPath(
          if (properties.getProperty("acoustic_model_path", "") != "") {
            "file://" + properties.getProperty("acoustic_model_path")
          }
          else {
            "resource:/edu/cmu/sphinx/models/en-us/en-us"
          })

        sphynxConfig.setDictionaryPath(
          if (properties.getProperty("dictionary_path", "") != "") {
            "file://" + properties.getProperty("dictionary_path")
          }
          else {
            "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict"
          })

        sphynxConfig.setLanguageModelPath(
          if (properties.getProperty("language_model_path", "") != "") {
            "file://" + properties.getProperty("language_model_path")
          } else {
            "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin"
          }
        )

        val recognizer = new StreamSpeechRecognizer(sphynxConfig)
        val stream = new FileInputStream(new File(config.in))

        // Stats class is used to collect speaker-specific data// Stats class is used to collect speaker-specific data

        val stats = recognizer.createStats(1)

        recognizer.startRecognition(stream)

        Stream.continually(recognizer.getResult)
          .takeWhile(_ ne null) foreach { result =>
          stats.collect(result)
        }

        recognizer.stopRecognition()

        recognizer.setTransform(stats.createTransform)

        stream.close()

        val nstream = new FileInputStream(new File(config.in))

        recognizer.startRecognition(nstream)

        val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config.out)))
        try {
          Stream.continually(recognizer.getResult)
            .takeWhile(_ ne null) foreach { result =>
              writer.write(result.getHypothesis)
              writer.write(' ')
          }
        }
        finally {
          writer.close()
          nstream.close()
          recognizer.stopRecognition()
        }
      }


    }

  }
}
