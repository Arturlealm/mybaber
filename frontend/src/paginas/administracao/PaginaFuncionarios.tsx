import { useState, type FormEvent } from 'react'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type { Funcionario, Pagina, TipoFuncionario } from '../../api/tiposApi'
import { AvisoErro, AvisoSucesso } from '../../componentes/AvisosOperacao'
import { formatarTelefone } from '../../compartilhado/formatadores'
import { aplicarMascaraTelefone } from '../../compartilhado/mascaras'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'
import { ModalJornadaBarbeiro } from './ModalJornadaBarbeiro'

const FORMULARIO_VAZIO = {
  nome: '',
  email: '',
  telefone: '',
  senha: '',
  tipo: 'BARBEIRO' as TipoFuncionario,
  realizaAtendimentos: true,
}

export function PaginaFuncionarios() {
  const funcionarios = useConsultaApi(
    () => requisitarApi<Pagina<Funcionario>>('/api/funcionarios', { parametros: { size: 100 } }),
    [],
  )
  const [formulario, setFormulario] = useState<typeof FORMULARIO_VAZIO | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)
  const [editandoHorario, setEditandoHorario] = useState<Funcionario | null>(null)

  async function cadastrar(evento: FormEvent) {
    evento.preventDefault()
    if (!formulario) return
    setErro(null)
    setSucesso(null)
    try {
      await requisitarApi('/api/funcionarios', { metodo: 'POST', corpo: formulario })
      setSucesso(`${formulario.nome} cadastrado. Ele já pode entrar pela opção "Sou da equipe".`)
      setFormulario(null)
      funcionarios.recarregar()
    } catch (falha) {
      setErro(descreverErro(falha))
    }
  }

  async function inativar(funcionario: Funcionario) {
    if (!window.confirm(`Inativar ${funcionario.nome}? Ele não poderá mais entrar nem receber agendamentos.`)) return
    setErro(null)
    setSucesso(null)
    try {
      await requisitarApi(`/api/funcionarios/${funcionario.id}`, { metodo: 'DELETE' })
      setSucesso(`${funcionario.nome} inativado.`)
      funcionarios.recarregar()
    } catch (falha) {
      setErro(descreverErro(falha))
    }
  }

  return (
    <div className="pilha">
      <div className="titulo-pagina">
        <div>
          <h1>Funcionários</h1>
          <p>Barbeiros e administradores da barbearia.</p>
        </div>
        <button type="button" className="botao" onClick={() => setFormulario(FORMULARIO_VAZIO)}>
          Novo funcionário
        </button>
      </div>

      <AvisoErro mensagem={erro ?? funcionarios.erro} />
      <AvisoSucesso mensagem={sucesso} />

      {formulario && (
        <form className="cartao pilha" onSubmit={cadastrar}>
          <h2>Novo funcionário</h2>
          <div className="grade-2">
            <label className="campo">
              Nome
              <input value={formulario.nome} onChange={(e) => setFormulario({ ...formulario, nome: e.target.value })} required />
            </label>
            <label className="campo">
              Email
              <input
                type="email"
                value={formulario.email}
                onChange={(e) => setFormulario({ ...formulario, email: e.target.value })}
                required
              />
            </label>
            <label className="campo">
              Telefone
              <input
                type="tel"
                value={formulario.telefone}
                inputMode="numeric"
                onChange={(e) => setFormulario({ ...formulario, telefone: aplicarMascaraTelefone(e.target.value) })}
                placeholder="(11) 91234-5678"
                required
              />
            </label>
            <label className="campo">
              Senha inicial
              <input
                type="password"
                value={formulario.senha}
                onChange={(e) => setFormulario({ ...formulario, senha: e.target.value })}
                minLength={8}
                required
                autoComplete="new-password"
              />
            </label>
            <label className="campo">
              Tipo
              <select
                value={formulario.tipo}
                onChange={(e) => setFormulario({ ...formulario, tipo: e.target.value as TipoFuncionario })}
              >
                <option value="BARBEIRO">Barbeiro</option>
                <option value="ADMINISTRADOR">Administrador</option>
              </select>
            </label>
            <label className="linha" style={{ alignItems: 'center' }}>
              <input
                type="checkbox"
                checked={formulario.realizaAtendimentos}
                onChange={(e) => setFormulario({ ...formulario, realizaAtendimentos: e.target.checked })}
              />
              Realiza atendimentos (aparece para os clientes agendarem)
            </label>
          </div>
          <div className="linha">
            <button type="submit" className="botao">
              Cadastrar
            </button>
            <button type="button" className="botao botao-secundario" onClick={() => setFormulario(null)}>
              Cancelar
            </button>
          </div>
        </form>
      )}

      <section className="cartao">
        <div className="tabela-container">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Contato</th>
                <th>Tipo</th>
                <th>Atende clientes</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {funcionarios.dados?.conteudo.map((funcionario) => (
                <tr key={funcionario.id}>
                  <td>
                    <strong>{funcionario.nome}</strong>
                  </td>
                  <td>
                    {funcionario.email}
                    <div className="campo-ajuda">{formatarTelefone(funcionario.telefone)}</div>
                  </td>
                  <td>{funcionario.tipo === 'ADMINISTRADOR' ? 'Administrador' : 'Barbeiro'}</td>
                  <td>{funcionario.realizaAtendimentos ? 'Sim' : 'Não'}</td>
                  <td className="numero">
                    <div className="linha" style={{ justifyContent: 'flex-end' }}>
                      {funcionario.realizaAtendimentos && (
                        <button
                          type="button"
                          className="botao botao-secundario botao-pequeno"
                          onClick={() => setEditandoHorario(funcionario)}
                        >
                          Horário
                        </button>
                      )}
                      <button type="button" className="botao botao-perigo botao-pequeno" onClick={() => inativar(funcionario)}>
                        Inativar
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {editandoHorario && (
        <ModalJornadaBarbeiro
          funcionario={editandoHorario}
          aoFechar={() => setEditandoHorario(null)}
          aoSalvar={(mensagem) => {
            setEditandoHorario(null)
            setSucesso(mensagem)
          }}
        />
      )}
    </div>
  )
}
