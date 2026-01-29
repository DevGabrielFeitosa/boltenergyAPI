package br.com.boltenergy.process_file_api.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table
class PowerPlant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var dataGeracaoConjuntoDeDados: LocalDate? = null,

    @Column(length = 10)
    var ideNucleoCEG: Integer? = null,

    @Column(length = 21)
    var codCEG: String? = null,

    @Column(length = 2)
    var sigUFPrincipal: String? = null,

    @Column(length = 50)
    var dscOrigemCombustivel: String? = null,

    @Column(length = 5)
    var sigTipoGeracao: String? = null,

    @Column(length = 255)
    var nomEmpreendimento: String? = null,

    var mdaPotenciaOutorgadaKw: Double? = null,

    @Column(length = 500)
    var dscPropriRegimePariticipacao: String? = null,

    @Column(length = 20)
    var dscViabilidade: String? = null,

    @Column(length = 20)
    var dscSituacaoObra: String? = null,

    @Column(length = 100)
    var dscJustificativaPrevisao: String? = null,

    var createdAt: LocalDate = LocalDate.now(),

    @Column(length = 500)
    var filename: String? = null
)