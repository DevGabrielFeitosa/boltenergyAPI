package br.com.boltenergy.process_file_api.controller

import br.com.boltenergy.process_file_api.dto.PagedResponse
import br.com.boltenergy.process_file_api.dto.TopGeneratorResponse
import br.com.boltenergy.process_file_api.dto.TopGeneratorsListResponse
import br.com.boltenergy.process_file_api.service.PowerPlantService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/power-plants")
class PowerPlantController(
    private val powerPlantService: PowerPlantService
) {

    @GetMapping("/top-generators")
    fun getTopGenerators(
        @RequestParam(defaultValue = "5") limit: Int
    ): ResponseEntity<TopGeneratorsListResponse> {
        val response = powerPlantService.getTopGenerators(limit)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getAllGenerators(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "mdaPotenciaOutorgadaKw") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<PagedResponse<TopGeneratorResponse>> {
        val response = powerPlantService.getAllGeneratorsPaged(page, size, sortBy, sortDirection, search)
        return ResponseEntity.ok(response)
    }
}
