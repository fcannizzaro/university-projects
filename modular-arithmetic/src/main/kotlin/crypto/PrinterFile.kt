package crypto

import java.io.PrintWriter

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
object PrinterFile {

    private var writer = PrintWriter("result.html")

    const val css = "body{background:#fff}#content{position:absolute;width:60%;left:20%;border-radius:10px;margin-top:16px;box-shadow:0 0 6px 1px rgba(0,0,0,0.15)}h3{border-top:1px solid #eee;padding:16px;color:#77a5b6;font-size:14px;margin-top:0}h3:first-of-type{border:none;padding-top:16px}p{font-size:14px;padding-left:24px;margin-bottom:24px}*{outline:none;font-family:'Roboto',sans-serif}.no-header{border:none}.no-space{font-size:14px;font-weight:bold;padding-bottom:4px;margin-bottom:0;color:#FF7E00}"

    private val template = "<html>" +
            "<head>" +
            "<style>$css</style>" +
            "<link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\">" +
            "<script src='https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.2/MathJax.js?config=TeX-MML-AM_CHTML'></script>" +
            "<script>MathJax.Hub.Config({ jax: [\"input/TeX\",\"output/HTML-CSS\"], displayAlign: \"left\"});</script>" +
            "</head>" +
            "<body><div id='content'>" +
            "xtextx" +
            "</div></body>" +
            "</html>"

    private var text = ""

    fun print(value: String, extra: String = "") {
        text += "<p class='$extra'>$$$value$$</p>"
        writer = PrintWriter("result.html")
        writer.println(template.replace("xtextx", text))
        writer.close()
    }

    fun header(value: String, extra: String = "") {
        text += "<h3 class='$extra'>$value</h3>"
        writer = PrintWriter("result.html")
        writer.println(template.replace("xtextx", text))
        writer.close()
    }

}