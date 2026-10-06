import { Navigate, Outlet } from 'react-router-dom'

import type { PerfilAcesso } from '../api/tiposApi'
import { rotaInicialDoPerfil, useAutenticacao } from './ContextoAutenticacao'

export function RotaProtegida({ perfisPermitidos }: { perfisPermitidos: PerfilAcesso[] }) {
  const { sessao } = useAutenticacao()

  if (!sessao) {
    return <Navigate to="/entrar" replace />
  }
  if (!perfisPermitidos.includes(sessao.perfil)) {
    return <Navigate to={rotaInicialDoPerfil(sessao.perfil)} replace />
  }
  return <Outlet />
}
