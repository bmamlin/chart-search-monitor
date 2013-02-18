import com.sun.net.httpserver.*
import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL
import javax.swing.WindowConstants as WC

def IP_ADDRESS = "localhost"
def DEFAULT_PORT = 8000

// Serve up Chart Search SOLR server data
class DataHandler implements HttpHandler {
  public void handle(HttpExchange t) throws IOException {
    byte[] response = new URL("http://10.3.200.112/Awesome3/dataimport?command=get-stats").text.bytes
    t.responseHeaders["Content-Type"] = "application/xml"
    t.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length)
    t.responseBody.write(response)
    t.close()
  }
}

// Serve up our single web page
class IndexHandler implements HttpHandler {
  public void handle(HttpExchange t) throws IOException {
    byte[] response = new File("ChartSearchMonitor.html").text.bytes
    t.responseHeaders["Content-Type"] = "text/html"
    t.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length)
    t.responseBody.write(response)
    t.close()
  }
}

// Port can be passed as sole parameter (defaults to 8000)
def port = 0
try {
  if (args.length > 0)
    port = args[0].toInteger()
} finally {
  if (port < 8000 || port > 49151)
    port = DEFAULT_PORT
}

// Start up our tiny little server
addr  = new InetSocketAddress(IP_ADDRESS, port)
server = HttpServer.create(addr, 10)
server.createContext("/data", new DataHandler())
server.createContext("/", new IndexHandler())
server.start()

// Swing a button to easily shut down the server
def myFrame
def sb = new SwingBuilder()
sb.edt {
  myFrame = frame(title:'Chart Search Monitor', size:[150,100], show:true, defaultCloseOperation:WC.DO_NOTHING_ON_CLOSE) {
    borderLayout()
    button(text:"Shut Down", actionPerformed: {
        server.stop(1)
        myFrame.visible = false
        System.exit(0)
    })
  }
}