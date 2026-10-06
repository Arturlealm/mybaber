import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'

import {
  EVENTO_SESSAO_EXPIRADA,
  lerSessaoSalva,
  removerSessao,
  requisitarApi,
  salvarSessao,
} from '../api/clienteHttp'
import type { PerfilAcesso, TokenAcesso } from '../api/tiposApi'

type TipoLogin = 'clientes' | 'funcionarios'

type ValorContextoAutenticacao = {
  sessao: TokenAcesso | null
  entrar: (tipo: TipoLogin, email: string, senha: string) => Promise<TokenAcesso>
  sair: () => void
}

const ContextoAutenticacao = createContext<ValorContextoAutenticacao | null>(null)

function lerSessaoValida(): TokenAcesso | null {
  const sessao = lerSessaoSalva<TokenAcesso>()
  if (!sessao || new Date(sessao.expiraEm).getTime() <= Date.now()) {
    removerSessao()
    return null
  }
  return sessao
}

export function ProvedorAutenticacao({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<TokenAcesso | null>(lerSessaoValida)

  const sair = useCallback(() => {
    removerSessao()
    setSessao(null)
  }, [])

  const entrar = useCallback(async (tipo: TipoLogin, email: string, senha: string) => {
    const token = await requisitarApi<TokenAcesso>(`/api/autenticacao/${tipo}/login`, {
      metodo: 'POST',
      corpo: { email, senha },
    })
    salvarSessao(token)
    setSessao(token)
    return token
  }, [])

  useEffect(() => {
    window.addEventListener(EVENTO_SESSAO_EXPIRADA, sair)
    return () => window.removeEventListener(EVENTO_SESSAO_EXPIRADA, sair)
  }, [sair])

  const valor = useMemo(() => ({ sessao, entrar, sair }), [sessao, entrar, sair])
  return <ContextoAutenticacao.Provider value={valor}>{children}</ContextoAutenticacao.Provider>
}

export function useAutenticacao() {
  const contexto = useContext(ContextoAutenticacao)
  if (!contexto) {
    throw new Error('useAutenticacao deve ser usado dentro de ProvedorAutenticacao')
  }
  return contexto
}

export function rotaInicialDoPerfil(perfil: PerfilAcesso) {
  if (perfil === 'CLIENTE') return '/agendar'
  if (perfil === 'BARBEIRO') return '/agenda-do-dia'
  return '/administracao/calendario'
}
