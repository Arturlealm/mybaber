import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'

import { descreverErro, requisitarApi } from '../api/clienteHttp'
import type { Cliente } from '../api/tiposApi'
import { useAutenticacao } from '../autenticacao/ContextoAutenticacao'
import { AvisoErro } from '../componentes/AvisosOperacao'

export function PaginaCadastroCliente() {
  const { entrar } = useAutenticacao()
  const navegar = useNavigate()
  const [formulario, setFormulario] = useState({ nome: '', email: '', telefone: '', cpf: '', senha: '' })
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  function alterar(campo: keyof typeof formulario, valor: string) {
    setFormulario((atual) => ({ ...atual, [campo]: valor }))
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      await requisitarApi<Cliente>('/api/clientes', {
        metodo: 'POST',
        corpo: { ...formulario, cpf: formulario.cpf || null },
      })
      await entrar('clientes', formulario.email, formulario.senha)
      navegar('/agendar')
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
          <h1>Criar conta</h1>
          <p className="texto-suave">Cadastre-se para agendar seu horário.</p>
        </div>
        <label className="campo">
          Nome
          <input value={formulario.nome} onChange={(e) => alterar('nome', e.target.value)} required autoComplete="name" />
        </label>
        <label className="campo">
          Email
          <input
            type="email"
            value={formulario.email}
            onChange={(e) => alterar('email', e.target.value)}
            required
            autoComplete="email"
          />
        </label>
        <label className="campo">
          Telefone
          <input
            type="tel"
            value={formulario.telefone}
            onChange={(e) => alterar('telefone', e.target.value)}
            placeholder="(11) 91234-5678"
            required
            autoComplete="tel"
          />
          <span className="campo-ajuda">Usado pela barbearia para falar com você, se necessário.</span>
        </label>
        <label className="campo">
          CPF <span className="campo-ajuda">(opcional)</span>
          <input value={formulario.cpf} onChange={(e) => alterar('cpf', e.target.value)} placeholder="000.000.000-00" />
        </label>
        <label className="campo">
          Senha
          <input
            type="password"
            value={formulario.senha}
            onChange={(e) => alterar('senha', e.target.value)}
            minLength={8}
            required
            autoComplete="new-password"
          />
          <span className="campo-ajuda">Mínimo de 8 caracteres.</span>
        </label>
        <AvisoErro mensagem={erro} />
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Criando conta...' : 'Criar conta'}
        </button>
        <p className="texto-suave">
          Já tem cadastro? <Link to="/entrar">Entrar</Link>
        </p>
      </form>
    </div>
  )
}
