package br.com.boltenergy.process_file_api.service

import br.com.boltenergy.process_file_api.entity.PowerPlant
import br.com.boltenergy.process_file_api.repository.PowerPlantRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Service
class CsvProcessingService(
    private val powerPlantRepository: PowerPlantRepository
) {
    private val logger = LoggerFactory.getLogger(CsvProcessingService::class.java)
    private val batchSize = 50000
    
    private val dateFormatters = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ISO_LOCAL_DATE
    )

    @Transactional
    fun processFile(file: File, fileName: String): ProcessingResult {
        logger.info("Iniciando processamento do arquivo: ${file.name}")
        val startTime = System.currentTimeMillis()

        var totalLines = 0
        var processedLines = 0
        var errorLines = 0
        val batch = mutableListOf<PowerPlant>()

        try {
            BufferedReader(InputStreamReader(FileInputStream(file), Charset.forName("ISO-8859-1"))).use { reader ->
                val header = reader.readLine()
                if (header == null) {
                    logger.error("Arquivo vazio ou sem cabeçalho")
                    return ProcessingResult(0, 0, 0, 0)
                }

                val columnIndexes = parseHeader(header)

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    totalLines++

                    try {
                        val powerPlant = parseLine(line!!, columnIndexes, fileName)
                        if (powerPlant != null) {
                            batch.add(powerPlant)

                            if (batch.size >= batchSize) {
                                powerPlantRepository.saveAll(batch)
                                processedLines += batch.size
                                batch.clear()

                                if (processedLines % 10000 == 0) {
                                    logger.info("Processadas $processedLines linhas...")
                                }
                            }
                        } else {
                            errorLines++
                        }
                    } catch (e: Exception) {
                        errorLines++
                        logger.warn("Erro ao processar linha $totalLines: ${e.message}")
                    }
                }

                if (batch.isNotEmpty()) {
                    powerPlantRepository.saveAll(batch)
                    processedLines += batch.size
                    batch.clear()
                }
            }

            val duration = (System.currentTimeMillis() - startTime) / 1000
            logger.info("Processamento concluído!")
            logger.info("Total de linhas: $totalLines")
            logger.info("Linhas processadas: $processedLines")
            logger.info("Linhas com erro: $errorLines")
            logger.info("Tempo de processamento: ${duration}s")

            return ProcessingResult(totalLines, processedLines, errorLines, duration)

        } catch (e: Exception) {
            logger.error("Erro ao processar arquivo: ${e.message}", e)
            throw e
        }
    }
    
    private fun parseHeader(header: String): Map<String, Int> {
        val columns = header.split(";")
        return columns.withIndex().associate { (index, name) -> name.trim() to index }
    }
    
    private fun parseLine(line: String, columnIndexes: Map<String, Int>, fileName: String): PowerPlant? {
        val values = line.split(";")

        if (values.isEmpty()) return null

        return PowerPlant(
            dataGeracaoConjuntoDeDados = parseDate(getValueOrNull(values, columnIndexes, "DatGeracaoConjuntoDados")),
            ideNucleoCEG = parseInteger(getValueOrNull(values, columnIndexes, "IdeNucleoCEG")),
            codCEG = getValueOrNull(values, columnIndexes, "CodCEG"),
            sigUFPrincipal = getValueOrNull(values, columnIndexes, "SigUFPrincipal"),
            dscOrigemCombustivel = getValueOrNull(values, columnIndexes, "DscOrigemCombustivel"),
            sigTipoGeracao = getValueOrNull(values, columnIndexes, "SigTipoGeracao"),
            nomEmpreendimento = getValueOrNull(values, columnIndexes, "NomEmpreendimento"),
            mdaPotenciaOutorgadaKw = parseDouble(getValueOrNull(values, columnIndexes, "MdaPotenciaOutorgadaKw")),
            dscPropriRegimePariticipacao = getValueOrNull(values, columnIndexes, "DscPropriRegimePariticipacao"),
            dscViabilidade = getValueOrNull(values, columnIndexes, "DscViabilidade"),
            dscSituacaoObra = getValueOrNull(values, columnIndexes, "DscSituacaoObra"),
            dscJustificativaPrevisao = getValueOrNull(values, columnIndexes, "DscJustificativaPrevisao"),
            filename = fileName
        )
    }
    
    private fun getValueOrNull(values: List<String>, columnIndexes: Map<String, Int>, columnName: String): String? {
        val index = columnIndexes[columnName] ?: return null
        if (index >= values.size) return null
        val value = values[index].trim()
        return if (value.isEmpty() || value == "null") null else value
    }
    
    private fun parseDate(dateStr: String?): LocalDate? {
        if (dateStr.isNullOrBlank()) return null
        
        for (formatter in dateFormatters) {
            try {
                return LocalDate.parse(dateStr, formatter)
            } catch (e: DateTimeParseException) {
            }
        }
        return null
    }
    
    private fun parseDouble(value: String?): Double? {
        if (value.isNullOrBlank()) return null
        return try {
            value.replace(",", ".").toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun parseInteger(value: String?): Integer? {
        if (value.isNullOrBlank()) return null
        return try {
            value.trim().toInt() as Integer
        } catch (e: NumberFormatException) {
            null
        }
    }

}

data class ProcessingResult(
    val totalLines: Int,
    val processedLines: Int,
    val errorLines: Int,
    val durationSeconds: Long
)