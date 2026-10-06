package com.mybarber.agenda;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agenda")
public class AgendaController {

    private final JornadaFuncionarioService jornadaFuncionarioService;
    private final AjusteAgendaService ajusteAgendaService;
    private final ExpedienteFuncionarioService expedienteFuncionarioService;

    public AgendaController(
            JornadaFuncionarioService jornadaFuncionarioService,
            AjusteAgendaService ajusteAgendaService,
            ExpedienteFuncionarioService expedienteFuncionarioService) {
        this.jornadaFuncionarioService = jornadaFuncionarioService;
        this.ajusteAgendaService = ajusteAgendaService;
        this.expedienteFuncionarioService = expedienteFuncionarioService;
    }

    @GetMapping("/calendario")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public List<CalendarioAgendaDiaResponse> montarCalendario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long filialId,
            @RequestParam(required = false) Long funcionarioId) {
        return expedienteFuncionarioService.montarCalendario(inicio, fim, filialId, funcionarioId);
    }

    @GetMapping("/jornadas/funcionarios/{funcionarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('BARBEIRO') and #jwt.subject == #funcionarioId.toString())")
    public JornadaFuncionarioResponse buscarJornada(
            @PathVariable Long funcionarioId,
            @AuthenticationPrincipal Jwt jwt) {
        return JornadaFuncionarioResponse.de(funcionarioId, jornadaFuncionarioService.buscarJornada(funcionarioId));
    }

    @PutMapping("/jornadas/funcionarios/{funcionarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public JornadaFuncionarioResponse definirJornada(
            @PathVariable Long funcionarioId,
            @Valid @RequestBody JornadaFuncionarioRequest requisicao) {
        return JornadaFuncionarioResponse.de(
                funcionarioId, jornadaFuncionarioService.definirJornada(funcionarioId, requisicao));
    }

    @GetMapping("/ajustes")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public List<AjusteAgendaResponse> listarAjustes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long filialId) {
        return ajusteAgendaService.listar(inicio, fim, filialId).stream().map(AjusteAgendaResponse::de).toList();
    }

    @PostMapping("/ajustes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AjusteAgendaResponse salvarAjuste(
            @Valid @RequestBody AjusteAgendaRequest requisicao,
            @AuthenticationPrincipal Jwt jwt) {
        return AjusteAgendaResponse.de(ajusteAgendaService.salvar(requisicao, Long.valueOf(jwt.getSubject())));
    }

    @DeleteMapping("/ajustes/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> removerAjuste(@PathVariable Long id) {
        ajusteAgendaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
