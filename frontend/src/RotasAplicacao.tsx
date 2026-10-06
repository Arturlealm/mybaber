import { Navigate, Route, Routes } from 'react-router-dom'

import { rotaInicialDoPerfil, useAutenticacao } from './autenticacao/ContextoAutenticacao'
import { RotaProtegida } from './autenticacao/RotaProtegida'
import { LayoutAplicacao } from './componentes/LayoutAplicacao'
import { PaginaMeusAgendamentos } from './paginas/cliente/PaginaMeusAgendamentos'
import { PaginaNovoAgendamento } from './paginas/cliente/PaginaNovoAgendamento'
import { PaginaCadastroCliente } from './paginas/PaginaCadastroCliente'
import { PaginaEntrar } from './paginas/PaginaEntrar'

function RedirecionamentoInicial() {
  const { sessao } = useAutenticacao()
  return <Navigate to={sessao ? rotaInicialDoPerfil(sessao.perfil) : '/entrar'} replace />
}

export function RotasAplicacao() {
  return (
    <Routes>
      <Route element={<LayoutAplicacao />}>
        <Route path="/entrar" element={<PaginaEntrar />} />
        <Route path="/cadastro" element={<PaginaCadastroCliente />} />

        <Route element={<RotaProtegida perfisPermitidos={['CLIENTE']} />}>
          <Route path="/agendar" element={<PaginaNovoAgendamento />} />
          <Route path="/meus-agendamentos" element={<PaginaMeusAgendamentos />} />
        </Route>

        <Route path="*" element={<RedirecionamentoInicial />} />
      </Route>
    </Routes>
  )
}
