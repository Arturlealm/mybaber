package com.mybarber.funcionario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mybarber.compartilhado.excecao.RegraNegocioException;
import com.mybarber.filial.FilialService;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private FilialService filialService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private FuncionarioService funcionarioService;

    @Test
    void deveImpedirQueAdministradorInativeASiMesmo() {
        assertThatThrownBy(() -> funcionarioService.inativar(1L, 1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível inativar o próprio usuário");
    }

    @Test
    void deveImpedirInativarUltimoAdministradorAtivo() {
        when(funcionarioRepository.findById(2L)).thenReturn(Optional.of(funcionario(TipoFuncionario.ADMINISTRADOR)));
        when(funcionarioRepository.countByTipoAndAtivoTrue(TipoFuncionario.ADMINISTRADOR)).thenReturn(1L);

        assertThatThrownBy(() -> funcionarioService.inativar(2L, 1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O sistema precisa ter pelo menos um administrador ativo");
    }

    @Test
    void deveInativarBarbeiro() {
        Funcionario barbeiro = funcionario(TipoFuncionario.BARBEIRO);
        when(funcionarioRepository.findById(2L)).thenReturn(Optional.of(barbeiro));

        funcionarioService.inativar(2L, 1L);

        assertThat(barbeiro.isAtivo()).isFalse();
    }

    @Test
    void deveImpedirRebaixarUltimoAdministradorParaBarbeiro() {
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(funcionario(TipoFuncionario.ADMINISTRADOR)));
        when(funcionarioRepository.existsByEmailAndIdNot("admin@email.com", 1L)).thenReturn(false);
        when(funcionarioRepository.countByTipoAndAtivoTrue(TipoFuncionario.ADMINISTRADOR)).thenReturn(1L);

        assertThatThrownBy(() -> funcionarioService.atualizar(1L, new FuncionarioAtualizacaoRequest(
                "Admin", "admin@email.com", null, "11912345678", TipoFuncionario.BARBEIRO, true, null)))
                .isInstanceOf(RegraNegocioException.class);
    }

    private Funcionario funcionario(TipoFuncionario tipo) {
        Funcionario funcionario = new Funcionario();
        funcionario.setTipo(tipo);
        return funcionario;
    }
}
