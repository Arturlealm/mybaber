import { useState, type FormEvent } from 'react'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type { Cliente } from '../../api/tiposApi'
import { AvisoErro } from '../../componentes/AvisosOperacao'
import { aplicarMascaraTelefone } from '../../compartilhado/mascaras'

type Propriedades = {
  buscaInicial: string
  aoCadastrar: (cliente: Cliente, senhaInicial: string) => void
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
      const resposta = await requisitarApi<{ cliente: Cliente; senhaInicial: string }>('/api/clientes/balcao', {
        metodo: 'POST',
        corpo: { nome, telefone, email },
      })
      aoCadastrar(resposta.cliente, resposta.senhaInicial)
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
        Email
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <span className="campo-ajuda">
          O cliente entra no site com este email e a senha padrão da barbearia.
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
