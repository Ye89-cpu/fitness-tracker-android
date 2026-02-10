package com.example.fitnesstracker

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

abstract class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, errorListener) {

    data class DataPart(
        val fileName: String,
        val content: ByteArray,
        val type: String = "application/octet-stream"
    )

    override fun getBodyContentType(): String = "multipart/form-data;boundary=$boundary"

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        try {
            // text params
            params?.forEach { (key, value) ->
                buildTextPart(dos, key, value)
            }

            // file params
            getByteData().forEach { (key, dataPart) ->
                buildDataPart(dos, dataPart, key)
            }

            dos.writeBytes("--$boundary--\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bos.toByteArray()
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: NetworkResponse) {
        listener.onResponse(response)
    }

    protected open fun getByteData(): Map<String, DataPart> = emptyMap()

    private fun buildTextPart(dos: DataOutputStream, name: String, value: String) {
        dos.writeBytes("--$boundary\r\n")
        dos.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
        dos.writeBytes("$value\r\n")
    }

    private fun buildDataPart(dos: DataOutputStream, dataFile: DataPart, inputName: String) {
        dos.writeBytes("--$boundary\r\n")
        dos.writeBytes(
            "Content-Disposition: form-data; name=\"$inputName\"; filename=\"${dataFile.fileName}\"\r\n"
        )
        dos.writeBytes("Content-Type: ${dataFile.type}\r\n\r\n")
        dos.write(dataFile.content)
        dos.writeBytes("\r\n")
    }

    companion object {
        private const val boundary = "apiclient-" + "boundary"
    }
}
