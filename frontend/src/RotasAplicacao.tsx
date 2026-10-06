import { Navigate, Route, Routes } from 'react-router-dom'

import { rotaInicialDoPerfil, useAutenticacao } from './autenticacao/ContextoAutenticacao'
import { LayoutAplicacao } from './componentes/LayoutAplicacao'
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
        <Route path="*" element={<RedirecionamentoInicial />} />
      </Route>
    </Routes>
  )
}
