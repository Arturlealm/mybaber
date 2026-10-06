package com.mybarber.relatorio;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Relatórios")
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioDesempenhoBarbeirosService relatorioDesempenhoBarbeirosService;

    public RelatorioController(RelatorioDesempenhoBarbeirosService relatorioDesempenhoBarbeirosService) {
        this.relatorioDesempenhoBarbeirosService = relatorioDesempenhoBarbeirosService;
    }

    @GetMapping("/desempenho-barbeiros")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public RelatorioDesempenhoBarbeirosResponse gerarDesempenhoBarbeiros(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long filialId) {
        return relatorioDesempenhoBarbeirosService.gerar(inicio, fim, filialId);
    }
}
