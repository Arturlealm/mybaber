import { useState, type FormEvent } from 'react'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type { Cliente } from '../../api/tiposApi'
import { AvisoErro } from '../../componentes/AvisosOperacao'
import { aplicarMascaraTelefone } from '../../compartilhado/mascaras'

type Propriedades = {
  buscaInicial: string
  aoCadastrar: (cliente: Cliente) => void
  aoCancelar: () => void
}

export function FormularioClienteBalcao({ buscaInicial, aoCadastrar, aoCancelar }: Propriedades) {
  const buscaEhTelefone = /\d/.test(buscaInicial) && !/[a-zA-ZÀ-ú]/.test(buscaInicial)
  const [nome, setNome] = useState(buscaEhTelefone ? '' : buscaInicial)
  const [telefone, setTelefone] = useState(buscaEhTelefone ? aplicarMascaraTelefone(buscaInicial) : '')
  const [email, setEmail] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function cadastrar(evento: FormEvent) {
    evento.preventDefault()
    evento.stopPropagation()
    setErro(null)
    setEnviando(true)
    try {
      const cliente = await requisitarApi<Cliente>('/api/clientes/balcao', {
        metodo: 'POST',
        corpo: { nome, telefone, email: email || null },
      })
      aoCadastrar(cliente)
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="pilha cartao" style={{ background: 'var(--cor-superficie-suave)' }} onSubmit={cadastrar}>
      <h3>Cadastrar cliente novo</h3>
      <label className="campo">
        Nome
        <input value={nome} onChange={(e) => setNome(e.target.value)} required autoFocus={!buscaEhTelefone || !nome} />
      </label>
      <label className="campo">
        Telefone
        <input
          type="tel"
          inputMode="numeric"
          value={telefone}
          onChange={(e) => setTelefone(aplicarMascaraTelefone(e.target.value))}
          placeholder="(11) 91234-5678"
          required
        />
      </label>
      <label className="campo">
        <span>
          Email <span className="campo-ajuda">(opcional)</span>
        </span>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <span className="campo-ajuda">
          Com o email, o cliente pode criar a senha depois em "Esqueci minha senha" e agendar pelo site.
        </span>
      </label>
      <AvisoErro mensagem={erro} />
      <div className="linha">
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Cadastrando...' : 'Cadastrar e selecionar'}
        </button>
        <button type="button" className="botao botao-secundario" onClick={aoCancelar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </form>
  )
}
