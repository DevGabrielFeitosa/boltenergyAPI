package br.com.boltenergy.process_file_api.repository

import br.com.boltenergy.process_file_api.entity.PowerPlant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PowerPlantRepository : JpaRepository<PowerPlant, Long> {}