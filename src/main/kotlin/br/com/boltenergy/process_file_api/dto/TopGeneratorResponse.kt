package br.com.boltenergy.process_file_api.dto

import java.time.LocalDate

data class TopGeneratorResponse(
    val ranking: Int,
    val id: Long?,
    val dataGeracaoConjuntoDeDados: LocalDate?,
    val ideNucleoCEG: Int?,
    val codCEG: String?,
    val sigUFPrincipal: String?,
    val dscOrigemCombustivel: String?,
    val sigTipoGeracao: String?,
    val nomEmpreendimento: String?,
    val mdaPotenciaOutorgadaKw: Double?,
    val dscPropriRegimePariticipacao: String?,
    val dscViabilidade: String?,
    val dscSituacaoObra: String?,
    val dscJustificativaPrevisao: String?
)

data class TopGeneratorsListResponse(
    val titulo: String,
    val totalRegistros: Int,
    val geradores: List<TopGeneratorResponse>
)

