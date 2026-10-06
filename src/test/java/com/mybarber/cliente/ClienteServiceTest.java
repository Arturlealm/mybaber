package com.mybarber.cliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mybarber.compartilhado.excecao.ConflitoDadosException;
import com.mybarber.compartilhado.excecao.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void deveCadastrarClienteNormalizandoDadosESemCpf() {
        when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senhaSegura123")).thenReturn("hash");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Cliente cliente = clienteService.cadastrar(new ClienteCadastroRequest(
                " João ", "JOAO@email.com", "", "(11) 91234-5678", "senhaSegura123"));

        assertThat(cliente.getNome()).isEqualTo("João");
        assertThat(cliente.getEmail()).isEqualTo("joao@email.com");
        assertThat(cliente.getCpf()).isNull();
        assertThat(cliente.getTelefone()).isEqualTo("11912345678");
        assertThat(cliente.getSenhaHash()).isEqualTo("hash");
        verify(clienteRepository, never()).existsByCpf(anyString());
    }

    @Test
    void deveImpedirCadastroComEmailDuplicado() {
        when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.cadastrar(new ClienteCadastroRequest(
                "João", "joao@email.com", null, "11912345678", "senhaSegura123")))
                .isInstanceOf(ConflitoDadosException.class)
                .hasMessage("Email já cadastrado");
    }

    @Test
    void deveImpedirCadastroComCpfDuplicado() {
        when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(clienteRepository.existsByCpf("52998224725")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.cadastrar(new ClienteCadastroRequest(
                "João", "joao@email.com", "529.982.247-25", "11912345678", "senhaSegura123")))
                .isInstanceOf(ConflitoDadosException.class)
                .hasMessage("CPF já cadastrado");
    }

    @Test
    void deveImpedirAtualizacaoComEmailDeOutroCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(new Cliente()));
        when(clienteRepository.existsByEmailAndIdNot("maria@email.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.atualizar(1L, new ClienteAtualizacaoRequest(
                "João", "maria@email.com", null, "11912345678")))
                .isInstanceOf(ConflitoDadosException.class)
                .hasMessage("Email já cadastrado para outro cliente");
    }

    @Test
    void deveImpedirAtualizacaoComCpfDeOutroCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(new Cliente()));
        when(clienteRepository.existsByEmailAndIdNot("joao@email.com", 1L)).thenReturn(false);
        when(clienteRepository.existsByCpfAndIdNot("52998224725", 1L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.atualizar(1L, new ClienteAtualizacaoRequest(
                "João", "joao@email.com", "52998224725", "11912345678")))
                .isInstanceOf(ConflitoDadosException.class)
                .hasMessage("CPF já cadastrado para outro cliente");
    }

    @Test
    void deveBuscarPorTelefoneQuandoABuscaContemApenasNumeros() {
        Pageable paginacao = Pageable.ofSize(20);
        when(clienteRepository.findAllByAtivoTrueAndTelefoneContaining("912345", paginacao)).thenReturn(Page.empty());

        clienteService.listarAtivos("91234-5", paginacao);

        verify(clienteRepository).findAllByAtivoTrueAndTelefoneContaining("912345", paginacao);
    }

    @Test
    void deveBuscarPorNomeQuandoABuscaContemLetras() {
        Pageable paginacao = Pageable.ofSize(20);
        when(clienteRepository.findAllByAtivoTrueAndNomeContainingIgnoreCase("João", paginacao)).thenReturn(Page.empty());

        clienteService.listarAtivos(" João ", paginacao);

        verify(clienteRepository).findAllByAtivoTrueAndNomeContainingIgnoreCase("João", paginacao);
    }

    @Test
    void deveInativarClienteEmVezDeExcluir() {
        Cliente cliente = new Cliente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.inativar(1L);

        assertThat(cliente.isAtivo()).isFalse();
        verify(clienteRepository, never()).deleteById(any());
    }

    @Test
    void deveTratarClienteInativoComoNaoEncontradoNasRotasDoProprioCliente() {
        Cliente cliente = new Cliente();
        cliente.setAtivo(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> clienteService.buscarAtivoPorId(1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
