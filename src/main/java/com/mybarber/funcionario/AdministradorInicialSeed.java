package com.mybarber.funcionario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdministradorInicialSeed implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdministradorInicialSeed.class);

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioService funcionarioService;
    private final AdministradorInicialPropriedades propriedades;

    public AdministradorInicialSeed(
            FuncionarioRepository funcionarioRepository,
            FuncionarioService funcionarioService,
            AdministradorInicialPropriedades propriedades) {
        this.funcionarioRepository = funcionarioRepository;
        this.funcionarioService = funcionarioService;
        this.propriedades = propriedades;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments argumentos) {
        if (funcionarioRepository.existsByTipoAndAtivoTrue(TipoFuncionario.ADMINISTRADOR)) {
            return;
        }

        if (isBlank(propriedades.email()) || isBlank(propriedades.senha()) || isBlank(propriedades.telefone())) {
            LOGGER.warn("Nenhum administrador ativo encontrado. Defina ADMIN_EMAIL, ADMIN_SENHA e ADMIN_TELEFONE para criar o administrador inicial");
            return;
        }

        funcionarioService.cadastrar(new FuncionarioCadastroRequest(
                isBlank(propriedades.nome()) ? "Administrador" : propriedades.nome(),
                propriedades.email(),
                null,
                propriedades.telefone(),
                propriedades.senha(),
                TipoFuncionario.ADMINISTRADOR,
                false,
                null));

        LOGGER.info("Administrador inicial criado com o email {}", propriedades.email());
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }
}
