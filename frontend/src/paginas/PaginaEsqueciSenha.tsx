import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'

import { descreverErro, requisitarApi } from '../api/clienteHttp'
import { AvisoErro, AvisoSucesso } from '../componentes/AvisosOperacao'

type TipoConta = 'CLIENTE' | 'FUNCIONARIO'

export function PaginaEsqueciSenha() {
  const [parametros] = useSearchParams()
  const [tipoConta, setTipoConta] = useState<TipoConta>(parametros.get('tipo') === 'equipe' ? 'FUNCIONARIO' : 'CLIENTE')
  const [email, setEmail] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [mensagem, setMensagem] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setMensagem(null)
    setEnviando(true)
    try {
      const resposta = await requisitarApi<{ mensagem: string }>('/api/autenticacao/redefinicao-senha/solicitacao', {
        metodo: 'POST',
        corpo: { email, tipoConta },
      })
      setMensagem(`${resposta.mensagem}. Verifique também a caixa de spam.`)
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="tela-acesso">
      <form className="cartao pilha" onSubmit={enviar}>
        <div>
          <h1>Esqueci minha senha</h1>
          <p className="texto-suave">Informe o email da sua conta para receber um link de redefinição.</p>
        </div>

        <div className="abas" role="tablist">
          <button
            type="button"
            role="tab"
            className="aba"
            aria-selected={tipoConta === 'CLIENTE'}
            onClick={() => setTipoConta('CLIENTE')}
          >
            Sou cliente
          </button>
          <button
            type="button"
            role="tab"
            className="aba"
            aria-selected={tipoConta === 'FUNCIONARIO'}
            onClick={() => setTipoConta('FUNCIONARIO')}
          >
            Sou da equipe
          </button>
        </div>

        <label className="campo">
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
        </label>
        <AvisoErro mensagem={erro} />
        <AvisoSucesso mensagem={mensagem} />
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Enviando...' : 'Enviar link'}
        </button>
        <p className="texto-suave">
          Lembrou a senha? <Link to="/entrar">Voltar para o login</Link>
        </p>
      </form>
    </div>
  )
}
