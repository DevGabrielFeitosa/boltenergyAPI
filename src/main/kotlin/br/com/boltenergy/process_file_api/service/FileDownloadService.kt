package br.com.boltenergy.process_file_api.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class FileDownloadService(
    @Value("\${file.download.url}") private val downloadUrl: String,
    @Value("\${file.download.directory}") private val downloadDirectory: String
) {
    private var jobInUse: Boolean = false
    private val logger = LoggerFactory.getLogger(FileDownloadService::class.java)
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build()

    @Scheduled(cron = "\${file.download.cron}")
    fun downloadFile() {

        if (jobInUse){
            logger.info("Job já em execução. Saltando execução atual...")
            return

        } else{
            logger.info("Iniciando download do arquivo de usinas da ANEEL...")
        }

        try {
            jobInUse = true
            val directory = File(downloadDirectory)

            if (!directory.exists()) {
                directory.mkdirs()
                logger.info("Diretório criado: $downloadDirectory")
            }

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"))
            val fileName = "ralie-usina_$timestamp.csv"
            val filePath = Paths.get(downloadDirectory, fileName)

            val request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .timeout(Duration.ofMinutes(5))
                .GET()
                .build()

            logger.info("Baixando arquivo de: $downloadUrl")
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())

            if (response.statusCode() == 200) {
                FileOutputStream(filePath.toFile()).use { outputStream ->
                    response.body().copyTo(outputStream)
                }

                logger.info("Download concluído com sucesso!")
                logger.info("Arquivo salvo em: $filePath")
            } else {
                logger.error("Erro ao baixar arquivo. Status code: ${response.statusCode()}")
            }

        } catch (e: Exception) {
            logger.error("Erro ao fazer download do arquivo: ${e.message}", e)
        } finally {
            jobInUse = false
        }
    }
}

