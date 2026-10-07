import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'

import { descreverErro, requisitarApi } from '../api/clienteHttp'
import { AvisoErro, AvisoSucesso } from '../componentes/AvisosOperacao'

export function PaginaRedefinirSenha() {
  const [parametros] = useSearchParams()
  const token = parametros.get('token')
  const [novaSenha, setNovaSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [mensagem, setMensagem] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    if (novaSenha !== confirmacao) {
      setErro('As senhas não são iguais.')
      return
    }
    setEnviando(true)
    try {
      const resposta = await requisitarApi<{ mensagem: string }>('/api/autenticacao/redefinicao-senha', {
        metodo: 'POST',
        corpo: { token, novaSenha },
      })
      setMensagem(resposta.mensagem)
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  if (!token) {
    return (
      <div className="tela-acesso">
        <div className="cartao pilha">
          <h1>Link inválido</h1>
          <p className="texto-suave">Abra o link enviado para o seu email ou solicite um novo.</p>
          <Link to="/esqueci-senha" className="botao">
            Solicitar novo link
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="tela-acesso">
      <form className="cartao pilha" onSubmit={enviar}>
        <div>
          <h1>Criar nova senha</h1>
          <p className="texto-suave">Escolha uma senha com pelo menos 8 caracteres.</p>
        </div>
        {mensagem ? (
          <>
            <AvisoSucesso mensagem={mensagem} />
            <Link to="/entrar" className="botao">
              Ir para o login
            </Link>
          </>
        ) : (
          <>
            <label className="campo">
              Nova senha
              <input
                type="password"
                value={novaSenha}
                onChange={(e) => setNovaSenha(e.target.value)}
                minLength={8}
                required
                autoComplete="new-password"
              />
            </label>
            <label className="campo">
              Confirme a nova senha
              <input
                type="password"
                value={confirmacao}
                onChange={(e) => setConfirmacao(e.target.value)}
                minLength={8}
                required
                autoComplete="new-password"
              />
            </label>
            <AvisoErro mensagem={erro} />
            {erro?.includes('expirou') && <Link to="/esqueci-senha">Solicitar novo link</Link>}
            <button type="submit" className="botao" disabled={enviando}>
              {enviando ? 'Salvando...' : 'Salvar nova senha'}
            </button>
          </>
        )}
      </form>
    </div>
  )
}
