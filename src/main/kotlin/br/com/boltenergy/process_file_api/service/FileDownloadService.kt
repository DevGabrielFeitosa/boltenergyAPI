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
    @Value("\${file.download.directory}") private val downloadDirectory: String,
    private val csvProcessingService: CsvProcessingService
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

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())

            if (response.statusCode() == 200) {
                val contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1)
                val fileSize = if (contentLength > 0) formatFileSize(contentLength) else "desconhecido"

                logger.info("Iniciando download do arquivo (Tamanho: $fileSize)...")

                FileOutputStream(filePath.toFile()).use { outputStream ->
                    downloadWithProgress(response.body(), outputStream, contentLength)
                }

                val result = csvProcessingService.processFile(filePath.toFile(), fileName)
                logger.info("Processamento concluído: ${result.processedLines} linhas processadas em ${result.durationSeconds}s")

            } else {
                logger.error("Erro ao baixar arquivo. Status code: ${response.statusCode()}")
            }

        } catch (e: Exception) {
            logger.error("Erro ao fazer download do arquivo: ${e.message}", e)
        } finally {
            jobInUse = false
        }
    }

    private fun downloadWithProgress(
        inputStream: java.io.InputStream,
        outputStream: FileOutputStream,
        totalBytes: Long
    ) {
        val buffer = ByteArray(8192)
        var bytesRead: Int
        var totalBytesRead = 0L
        val startTime = System.currentTimeMillis()
        var lastProgressReported = 0

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead

            if (totalBytes > 0) {
                val currentProgress = (totalBytesRead * 100.0 / totalBytes).toInt()

                if (currentProgress >= lastProgressReported + 20 || currentProgress == 100) {
                    val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
                    val speed = if (elapsedTime > 0) totalBytesRead / elapsedTime else 0.0
                    val eta = if (speed > 0) ((totalBytes - totalBytesRead) / speed).toLong() else 0

                    val progressBar = createProgressBar(currentProgress)
                    logger.info(
                        String.format(
                            "Download: %s %d%% | %s / %s | %s/s | ETA: %s",
                            progressBar,
                            currentProgress,
                            formatFileSize(totalBytesRead),
                            formatFileSize(totalBytes),
                            formatFileSize(speed.toLong()),
                            formatTime(eta)
                        )
                    )

                    lastProgressReported = currentProgress
                }
            }
        }
    }

    private fun createProgressBar(percent: Int): String {
        val barLength = 30
        val filled = (barLength * percent / 100).coerceIn(0, barLength)
        val empty = barLength - filled
        return "[" + "█".repeat(filled) + "░".repeat(empty) + "]"
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    private fun formatTime(seconds: Long): String {
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> String.format("%dm %ds", seconds / 60, seconds % 60)
            else -> String.format("%dh %dm", seconds / 3600, (seconds % 3600) / 60)
        }
    }
}

