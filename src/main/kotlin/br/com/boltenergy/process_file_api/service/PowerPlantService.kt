package br.com.boltenergy.process_file_api.service

import br.com.boltenergy.process_file_api.dto.TopGeneratorResponse
import br.com.boltenergy.process_file_api.dto.TopGeneratorsListResponse
import br.com.boltenergy.process_file_api.repository.PowerPlantRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class PowerPlantService(
    private val powerPlantRepository: PowerPlantRepository
) {
    private val logger = LoggerFactory.getLogger(PowerPlantService::class.java)

    fun getTopGenerators(limit: Int = 5): TopGeneratorsListResponse {
        logger.info("GetTopGenerators")

        val pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "mdaPotenciaOutorgadaKw"))
        val topGenerators = powerPlantRepository.findAll(pageable)

        val generatorsList = topGenerators.content
            .filter { it.mdaPotenciaOutorgadaKw != null && it.mdaPotenciaOutorgadaKw!! > 0 }
            .mapIndexed { index, powerPlant ->
                TopGeneratorResponse(
                    ranking = index + 1,
                    id = powerPlant.id,
                    dataGeracaoConjuntoDeDados = powerPlant.dataGeracaoConjuntoDeDados,
                    ideNucleoCEG = powerPlant.ideNucleoCEG?.toInt(),
                    codCEG = powerPlant.codCEG,
                    sigUFPrincipal = powerPlant.sigUFPrincipal,
                    dscOrigemCombustivel = powerPlant.dscOrigemCombustivel,
                    sigTipoGeracao = powerPlant.sigTipoGeracao,
                    nomEmpreendimento = powerPlant.nomEmpreendimento,
                    mdaPotenciaOutorgadaKw = powerPlant.mdaPotenciaOutorgadaKw,
                    dscPropriRegimePariticipacao = powerPlant.dscPropriRegimePariticipacao,
                    dscViabilidade = powerPlant.dscViabilidade,
                    dscSituacaoObra = powerPlant.dscSituacaoObra,
                    dscJustificativaPrevisao = powerPlant.dscJustificativaPrevisao
                )
            }

        logger.info("Response example: ${generatorsList.size}")

        return TopGeneratorsListResponse(
            titulo = "Top $limit Maiores Geradores de Energia do Brasil",
            totalRegistros = generatorsList.size,
            geradores = generatorsList
        )
    }
}

