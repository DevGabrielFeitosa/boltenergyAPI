package br.com.boltenergy.process_file_api.controller

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
}
