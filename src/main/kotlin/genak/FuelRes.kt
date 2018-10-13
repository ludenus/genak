package genak

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result


class FuelRes(triplet: Triple<Request, Response, Result<String, FuelError>>) {

    val request: Request = triplet.first
    val response: Response = triplet.second
    val result: Result<String, FuelError> = triplet.third

    fun resolve(): String {
        val (str, err) = this.result
        return when {
            err != null -> err.message!!
            str != null -> str
            else -> throw IllegalStateException("neither String nor FuelError, how come?")
        }
    }
}