import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'

import { descreverErro } from '../api/clienteHttp'
import { rotaInicialDoPerfil, useAutenticacao } from '../autenticacao/ContextoAutenticacao'
import { AvisoErro } from '../componentes/AvisosOperacao'

type TipoAcesso = 'clientes' | 'funcionarios'

export function PaginaEntrar() {
  const { sessao, entrar } = useAutenticacao()
  const navegar = useNavigate()
  const [tipoAcesso, setTipoAcesso] = useState<TipoAcesso>('clientes')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (sessao) {
    return <Navigate to={rotaInicialDoPerfil(sessao.perfil)} replace />
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      const token = await entrar(tipoAcesso, email, senha)
      navegar(rotaInicialDoPerfil(token.perfil))
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="tela-acesso">
      <div className="cartao pilha">
        <div>
          <h1>Entrar</h1>
          <p className="texto-suave">Acesse para agendar ou gerenciar a barbearia.</p>
        </div>

        <div className="abas" role="tablist">
          <button
            type="button"
            role="tab"
            className="aba"
            aria-selected={tipoAcesso === 'clientes'}
            onClick={() => setTipoAcesso('clientes')}
          >
            Sou cliente
          </button>
          <button
            type="button"
            role="tab"
            className="aba"
            aria-selected={tipoAcesso === 'funcionarios'}
            onClick={() => setTipoAcesso('funcionarios')}
          >
            Sou da equipe
          </button>
        </div>

        <form className="pilha" onSubmit={enviar}>
          <label className="campo">
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
          </label>
          <label className="campo">
            Senha
            <input
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              required
              autoComplete="current-password"
            />
          </label>
          <AvisoErro mensagem={erro} />
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? 'Entrando...' : 'Entrar'}
          </button>
        </form>

        <p className="texto-suave" style={{ margin: 0 }}>
          <Link to={`/esqueci-senha?tipo=${tipoAcesso === 'clientes' ? 'cliente' : 'equipe'}`}>Esqueci minha senha</Link>
        </p>

        {tipoAcesso === 'clientes' && (
          <p className="texto-suave">
            Ainda não tem cadastro? <Link to="/cadastro">Crie sua conta</Link>
          </p>
        )}
      </div>
    </div>
  )
}
