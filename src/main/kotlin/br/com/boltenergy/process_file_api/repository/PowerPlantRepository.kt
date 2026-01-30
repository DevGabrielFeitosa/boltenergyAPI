package br.com.boltenergy.process_file_api.repository

import br.com.boltenergy.process_file_api.entity.PowerPlant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PowerPlantRepository : JpaRepository<PowerPlant, Long> {

    @Query("""
        SELECT p FROM PowerPlant p
        WHERE p.mdaPotenciaOutorgadaKw > 0
        AND (
            LOWER(p.nomEmpreendimento) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.sigUFPrincipal) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.sigTipoGeracao) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.dscOrigemCombustivel) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    fun findAllWithSearch(@Param("search") search: String, pageable: Pageable): Page<PowerPlant>

    @Query("SELECT p FROM PowerPlant p WHERE p.mdaPotenciaOutorgadaKw > 0")
    fun findAllWithPowerGreaterThanZero(pageable: Pageable): Page<PowerPlant>
}