package genak

import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient.Response
import org.slf4j.LoggerFactory


class AHttp {
    val log = LoggerFactory.getLogger(this.javaClass.name)

    var client = asyncHttpClient()


    fun httpGet(url: String) = client
            .prepareGet(url)
            .execute(
                    object : AsyncCompletionHandler<Response>() {

                        override fun onCompleted(response: Response): Response {
                            log.debug("4 {}", response)
                            return response
                        }

                        override fun onThrowable(t: Throwable) {
                            log.error("-1", t)
                            throw t
                        }
                    }).get()

    fun httpGetFuture(url: String) = client
            .prepareGet(url)
            .execute(
                    object : AsyncCompletionHandler<Response>() {

                        override fun onCompleted(response: Response): Response {
                            log.debug("4 {}", response)
                            return response
                        }

                        override fun onThrowable(t: Throwable) {
                            log.error("-1", t)
                            throw t
                        }
                    })

}