import { NavLink, Outlet, useNavigate } from 'react-router-dom'

import type { PerfilAcesso } from '../api/tiposApi'
import { useAutenticacao } from '../autenticacao/ContextoAutenticacao'

type ItemMenu = { rota: string; rotulo: string }

const MENU_POR_PERFIL: Record<PerfilAcesso, ItemMenu[]> = {
  CLIENTE: [
    { rota: '/agendar', rotulo: 'Agendar' },
    { rota: '/meus-agendamentos', rotulo: 'Meus agendamentos' },
  ],
  BARBEIRO: [{ rota: '/agenda-do-dia', rotulo: 'Agenda do dia' }],
  ADMINISTRADOR: [
    { rota: '/administracao/calendario', rotulo: 'Calendário' },
    { rota: '/agenda-do-dia', rotulo: 'Agenda do dia' },
    { rota: '/administracao/servicos', rotulo: 'Serviços' },
    { rota: '/administracao/funcionarios', rotulo: 'Funcionários' },
    { rota: '/administracao/relatorios', rotulo: 'Relatórios' },
  ],
}

const NOME_PERFIL: Record<PerfilAcesso, string> = {
  CLIENTE: 'Cliente',
  BARBEIRO: 'Barbeiro',
  ADMINISTRADOR: 'Administrador',
}

export function LayoutAplicacao() {
  const { sessao, sair } = useAutenticacao()
  const navegar = useNavigate()

  function encerrarSessao() {
    sair()
    navegar('/entrar')
  }

  return (
    <div className="layout">
      <header className="cabecalho">
        <div className="cabecalho-conteudo">
          <span className="marca">
            <img src="/favicon.svg" alt="" width={28} height={28} />
            MyBarber
          </span>
          {sessao && (
            <nav className="menu">
              {MENU_POR_PERFIL[sessao.perfil].map((item) => (
                <NavLink key={item.rota} to={item.rota} className="menu-item">
                  {item.rotulo}
                </NavLink>
              ))}
            </nav>
          )}
          {sessao && (
            <div className="usuario">
              <span>
                {sessao.nome} <small>{NOME_PERFIL[sessao.perfil]}</small>
              </span>
              <button type="button" className="botao botao-fantasma" onClick={encerrarSessao}>
                Sair
              </button>
            </div>
          )}
        </div>
      </header>
      <main className="conteudo">
        <Outlet />
      </main>
    </div>
  )
}
