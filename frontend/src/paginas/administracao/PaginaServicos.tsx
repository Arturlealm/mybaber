import { useState, type FormEvent } from 'react'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type { CombinacaoServico, ServicoOferecido } from '../../api/tiposApi'
import { AvisoErro, AvisoSucesso } from '../../componentes/AvisosOperacao'
import { formatarDuracao, formatarMoeda } from '../../compartilhado/formatadores'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'

type FormularioServico = { id: number | null; nome: string; descricao: string; duracaoMinutos: string; preco: string }

const FORMULARIO_SERVICO_VAZIO: FormularioServico = { id: null, nome: '', descricao: '', duracaoMinutos: '30', preco: '' }

export function PaginaServicos() {
  const servicos = useConsultaApi(() => requisitarApi<ServicoOferecido[]>('/api/servicos'), [])
  const combinacoes = useConsultaApi(() => requisitarApi<CombinacaoServico[]>('/api/servicos/combinacoes'), [])
  const [formularioServico, setFormularioServico] = useState<FormularioServico | null>(null)
  const [novaCombinacao, setNovaCombinacao] = useState<{ nome: string; servicoIds: number[] } | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)

  async function executar(operacao: () => Promise<unknown>, mensagem: string) {
    setErro(null)
    setSucesso(null)
    try {
      await operacao()
      setSucesso(mensagem)
      servicos.recarregar()
      combinacoes.recarregar()
      return true
    } catch (falha) {
      setErro(descreverErro(falha))
      return false
    }
  }

  async function salvarServico(evento: FormEvent) {
    evento.preventDefault()
    if (!formularioServico) return
    const corpo = {
      nome: formularioServico.nome,
      descricao: formularioServico.descricao || null,
      duracaoMinutos: Number(formularioServico.duracaoMinutos),
      preco: Number(formularioServico.preco.replace(',', '.')),
    }
    const salvo = await executar(
      () =>
        formularioServico.id
          ? requisitarApi(`/api/servicos/${formularioServico.id}`, { metodo: 'PUT', corpo })
          : requisitarApi('/api/servicos', { metodo: 'POST', corpo }),
      'Serviço salvo.',
    )
    if (salvo) setFormularioServico(null)
  }

  async function salvarCombinacao(evento: FormEvent) {
    evento.preventDefault()
    if (!novaCombinacao) return
    const salvo = await executar(
      () => requisitarApi('/api/servicos/combinacoes', { metodo: 'POST', corpo: novaCombinacao }),
      'Opção de serviço criada.',
    )
    if (salvo) setNovaCombinacao(null)
  }

  function alternarServicoNaCombinacao(servicoId: number) {
    setNovaCombinacao((atual) =>
      atual
        ? {
            ...atual,
            servicoIds: atual.servicoIds.includes(servicoId)
              ? atual.servicoIds.filter((id) => id !== servicoId)
              : [...atual.servicoIds, servicoId],
          }
        : atual,
    )
  }

  return (
    <div className="pilha">
      <div className="titulo-pagina">
        <div>
          <h1>Serviços</h1>
          <p>Preços e durações usados nos agendamentos. O pagamento é feito na barbearia.</p>
        </div>
      </div>

      <AvisoErro mensagem={erro ?? servicos.erro ?? combinacoes.erro} />
      <AvisoSucesso mensagem={sucesso} />

      <section className="cartao pilha">
        <div className="item-lista">
          <h2>Serviços da casa</h2>
          <button type="button" className="botao botao-pequeno" onClick={() => setFormularioServico(FORMULARIO_SERVICO_VAZIO)}>
            Novo serviço
          </button>
        </div>
        <div className="tabela-container">
          <table>
            <thead>
              <tr>
                <th>Serviço</th>
                <th>Duração</th>
                <th className="numero">Preço</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {servicos.dados?.map((servico) => (
                <tr key={servico.id}>
                  <td>
                    <strong>{servico.nome}</strong>
                    {servico.descricao && <div className="campo-ajuda">{servico.descricao}</div>}
                  </td>
                  <td>{formatarDuracao(servico.duracaoMinutos)}</td>
                  <td className="numero">{formatarMoeda(servico.preco)}</td>
                  <td className="numero">
                    <div className="linha" style={{ justifyContent: 'flex-end' }}>
                      <button
                        type="button"
                        className="botao botao-secundario botao-pequeno"
                        onClick={() =>
                          setFormularioServico({
                            id: servico.id,
                            nome: servico.nome,
                            descricao: servico.descricao ?? '',
                            duracaoMinutos: String(servico.duracaoMinutos),
                            preco: String(servico.preco),
                          })
                        }
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        className="botao botao-perigo botao-pequeno"
                        onClick={() =>
                          window.confirm(`Desativar o serviço ${servico.nome}?`) &&
                          executar(() => requisitarApi(`/api/servicos/${servico.id}`, { metodo: 'DELETE' }), 'Serviço desativado.')
                        }
                      >
                        Desativar
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {formularioServico && (
          <form className="pilha cartao" style={{ background: 'var(--cor-superficie-suave)' }} onSubmit={salvarServico}>
            <h3>{formularioServico.id ? 'Editar serviço' : 'Novo serviço'}</h3>
            <div className="linha">
              <label className="campo" style={{ flex: 2 }}>
                Nome
                <input
                  value={formularioServico.nome}
                  onChange={(e) => setFormularioServico({ ...formularioServico, nome: e.target.value })}
                  required
                />
              </label>
              <label className="campo" style={{ flex: 1 }}>
                Duração (min)
                <input
                  type="number"
                  min={0}
                  step={5}
                  value={formularioServico.duracaoMinutos}
                  onChange={(e) => setFormularioServico({ ...formularioServico, duracaoMinutos: e.target.value })}
                  required
                />
              </label>
              <label className="campo" style={{ flex: 1 }}>
                Preço (R$)
                <input
                  inputMode="decimal"
                  value={formularioServico.preco}
                  onChange={(e) => setFormularioServico({ ...formularioServico, preco: e.target.value })}
                  required
                />
              </label>
            </div>
            <label className="campo">
              <span>
                Descrição <span className="campo-ajuda">(opcional)</span>
              </span>
              <input
                value={formularioServico.descricao}
                onChange={(e) => setFormularioServico({ ...formularioServico, descricao: e.target.value })}
              />
            </label>
            <div className="linha">
              <button type="submit" className="botao">
                Salvar
              </button>
              <button type="button" className="botao botao-secundario" onClick={() => setFormularioServico(null)}>
                Cancelar
              </button>
            </div>
          </form>
        )}
      </section>

      <section className="cartao pilha">
        <div className="item-lista">
          <div>
            <h2>Opções para o cliente</h2>
            <p className="texto-suave">O que aparece em "Qual será o serviço?" na hora de agendar.</p>
          </div>
          <button type="button" className="botao botao-pequeno" onClick={() => setNovaCombinacao({ nome: '', servicoIds: [] })}>
            Nova opção
          </button>
        </div>
        <ul className="lista">
          {combinacoes.dados?.map((combinacao) => (
            <li key={combinacao.id} className="item-lista">
              <div>
                <strong>{combinacao.nome}</strong>
                <div className="texto-suave">
                  {combinacao.servicos.map((servico) => servico.nome).join(' + ')} ·{' '}
                  {formatarDuracao(combinacao.duracaoTotalMinutos)} · {formatarMoeda(combinacao.precoTotal)}
                </div>
              </div>
              <button
                type="button"
                className="botao botao-perigo botao-pequeno"
                onClick={() =>
                  window.confirm(`Desativar a opção ${combinacao.nome}?`) &&
                  executar(
                    () => requisitarApi(`/api/servicos/combinacoes/${combinacao.id}`, { metodo: 'DELETE' }),
                    'Opção desativada.',
                  )
                }
              >
                Desativar
              </button>
            </li>
          ))}
        </ul>

        {novaCombinacao && (
          <form className="pilha cartao" style={{ background: 'var(--cor-superficie-suave)' }} onSubmit={salvarCombinacao}>
            <h3>Nova opção</h3>
            <label className="campo">
              Nome exibido ao cliente
              <input
                value={novaCombinacao.nome}
                onChange={(e) => setNovaCombinacao({ ...novaCombinacao, nome: e.target.value })}
                placeholder="Ex.: Cabelo, barba e pigmentação"
                required
              />
            </label>
            <div className="linha">
              {servicos.dados?.map((servico) => (
                <label key={servico.id} className="opcao-marcavel">
                  <input
                    type="checkbox"
                    checked={novaCombinacao.servicoIds.includes(servico.id)}
                    onChange={() => alternarServicoNaCombinacao(servico.id)}
                  />
                  {servico.nome}
                </label>
              ))}
            </div>
            <div className="linha">
              <button type="submit" className="botao" disabled={novaCombinacao.servicoIds.length === 0}>
                Salvar
              </button>
              <button type="button" className="botao botao-secundario" onClick={() => setNovaCombinacao(null)}>
                Cancelar
              </button>
            </div>
          </form>
        )}
      </section>
    </div>
  )
}
