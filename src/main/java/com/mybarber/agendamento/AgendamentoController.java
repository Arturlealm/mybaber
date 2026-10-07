package com.mybarber.agendamento;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mybarber.autenticacao.UsuarioAutenticado;
import com.mybarber.compartilhado.paginacao.PaginaResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Agendamentos")
@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final HorarioDisponivelService horarioDisponivelService;

    public AgendamentoController(
            AgendamentoService agendamentoService,
            HorarioDisponivelService horarioDisponivelService) {
        this.agendamentoService = agendamentoService;
        this.horarioDisponivelService = horarioDisponivelService;
    }

    @GetMapping("/horarios-disponiveis")
    public HorariosDisponiveisResponse listarHorariosDisponiveis(
            @RequestParam Long funcionarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam List<Long> servicoIds) {
        return horarioDisponivelService.listar(funcionarioId, data, servicoIds);
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponse> criar(
            @Valid @RequestBody AgendamentoCriacaoRequest requisicao,
            @AuthenticationPrincipal Jwt jwt) {
        Agendamento agendamento = agendamentoService.criar(requisicao, UsuarioAutenticado.de(jwt));
        return ResponseEntity.created(URI.create("/api/agendamentos/" + agendamento.getId()))
                .body(AgendamentoResponse.de(agendamento));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public PaginaResponse<AgendamentoResponse> listarDoClienteAutenticado(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20) Pageable paginacao) {
        return PaginaResponse.de(
                agendamentoService.listarDoCliente(Long.valueOf(jwt.getSubject()), paginacao),
                AgendamentoResponse::de);
    }

    @GetMapping("/agenda-do-dia")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public List<AgendamentoResponse> listarAgendaDoDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) Long filialId,
            @RequestParam(required = false) Long funcionarioId,
            @AuthenticationPrincipal Jwt jwt) {
        return agendamentoService.listarAgendaDoDia(data, filialId, funcionarioId, UsuarioAutenticado.de(jwt))
                .stream()
                .map(AgendamentoResponse::de)
                .toList();
    }

    @GetMapping("/periodo")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public List<AgendamentoResponse> listarAgendaDoPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long filialId,
            @RequestParam(required = false) Long funcionarioId,
            @AuthenticationPrincipal Jwt jwt) {
        return agendamentoService.listarAgendaDoPeriodo(inicio, fim, filialId, funcionarioId, UsuarioAutenticado.de(jwt))
                .stream()
                .map(AgendamentoResponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    public AgendamentoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return AgendamentoResponse.de(agendamentoService.buscar(id, UsuarioAutenticado.de(jwt)));
    }

    @PatchMapping("/{id}/cancelamento")
    public AgendamentoResponse cancelar(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AgendamentoCancelamentoRequest requisicao,
            @AuthenticationPrincipal Jwt jwt) {
        return AgendamentoResponse.de(agendamentoService.cancelar(id, requisicao, UsuarioAutenticado.de(jwt)));
    }

    @PatchMapping("/{id}/conclusao")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public AgendamentoResponse concluir(
            @PathVariable Long id,
            @Valid @RequestBody AgendamentoConclusaoRequest requisicao,
            @AuthenticationPrincipal Jwt jwt) {
        return AgendamentoResponse.de(agendamentoService.concluir(id, requisicao, UsuarioAutenticado.de(jwt)));
    }

    @PatchMapping("/{id}/nao-comparecimento")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public AgendamentoResponse registrarNaoComparecimento(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return AgendamentoResponse.de(agendamentoService.registrarNaoComparecimento(id, UsuarioAutenticado.de(jwt)));
    }
}
