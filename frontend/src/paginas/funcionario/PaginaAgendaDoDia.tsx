import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type { Agendamento, BarbeiroResumo } from '../../api/tiposApi'
import { useAutenticacao } from '../../autenticacao/ContextoAutenticacao'
import { AvisoErro, AvisoSucesso } from '../../componentes/AvisosOperacao'
import {
  capitalizar,
  formatarDataLonga,
  formatarHora,
  formatarMoeda,
  formatarTelefone,
  hojeIso,
  somarDias,
} from '../../compartilhado/formatadores'
import { NOME_STATUS_AGENDAMENTO } from '../../compartilhado/nomesExibicao'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'
import { ModalAgendamentoBalcao } from './ModalAgendamentoBalcao'
import { ModalConclusaoAtendimento } from './ModalConclusaoAtendimento'

export function PaginaAgendaDoDia() {
  const { sessao } = useAutenticacao()
  const ehAdministrador = sessao?.perfil === 'ADMINISTRADOR'
  const [parametros] = useSearchParams()
  const [data, setData] = useState(() => {
    const dataInformada = parametros.get('data')
    return dataInformada && /^\d{4}-\d{2}-\d{2}$/.test(dataInformada) ? dataInformada : hojeIso()
  })
  const [funcionarioId, setFuncionarioId] = useState('')
  const [emConclusao, setEmConclusao] = useState<Agendamento | null>(null)
  const [agendandoNoBalcao, setAgendandoNoBalcao] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)

  const barbeiros = useConsultaApi(
    ehAdministrador ? () => requisitarApi<BarbeiroResumo[]>('/api/funcionarios/barbeiros') : null,
    [ehAdministrador],
  )
  const agenda = useConsultaApi(
    () =>
      requisitarApi<Agendamento[]>('/api/agendamentos/agenda-do-dia', {
        parametros: { data, funcionarioId: funcionarioId || undefined },
      }),
    [data, funcionarioId],
  )

  const agendamentos = agenda.dados ?? []
  const concluidos = agendamentos.filter((item) => item.status === 'CONCLUIDO')
  const totalDoDia = concluidos.reduce((soma, item) => soma + (item.valorCobrado ?? 0), 0)

  async function executarAcao(agendamento: Agendamento, acao: 'nao-comparecimento' | 'cancelamento') {
    const pergunta =
      acao === 'cancelamento'
        ? `Cancelar o horário de ${agendamento.cliente.nome} às ${formatarHora(agendamento.inicio)}?`
        : `Registrar que ${agendamento.cliente.nome} não compareceu?`
    if (!window.confirm(pergunta)) return

    setErro(null)
    setSucesso(null)
    try {
      await requisitarApi(`/api/agendamentos/${agendamento.id}/${acao}`, { metodo: 'PATCH', corpo: {} })
      setSucesso(acao === 'cancelamento' ? 'Agendamento cancelado.' : 'Não comparecimento registrado.')
      agenda.recarregar()
    } catch (falha) {
      setErro(descreverErro(falha))
    }
  }

  function aoAgendarNoBalcao() {
    setAgendandoNoBalcao(false)
    setSucesso('Agendamento criado.')
    agenda.recarregar()
  }

  function aoConcluirAtendimento() {
    setEmConclusao(null)
    setSucesso('Atendimento concluído.')
    agenda.recarregar()
  }

  return (
    <div className="pilha">
      <div className="titulo-pagina">
        <div>
          <h1>Agenda do dia</h1>
          <p>{capitalizar(formatarDataLonga(data))}</p>
        </div>
        <div className="barra-ferramentas">
          <div className="grupo-ferramentas">
            <button type="button" className="botao" onClick={() => setAgendandoNoBalcao(true)}>
              Novo agendamento
            </button>
          </div>
          <div className="grupo-ferramentas">
            <button type="button" className="botao botao-secundario" onClick={() => setData(somarDias(data, -1))}>
              ‹ Anterior
            </button>
            <button type="button" className="botao botao-secundario" onClick={() => setData(hojeIso())}>
              Hoje
            </button>
            <button type="button" className="botao botao-secundario" onClick={() => setData(somarDias(data, 1))}>
              Próximo ›
            </button>
          </div>
          <div className="grupo-ferramentas">
            <label className="campo">
              <input
                type="date"
                aria-label="Data"
                value={data}
                onChange={(e) => e.target.value && setData(e.target.value)}
              />
            </label>
            {ehAdministrador && (
              <label className="campo">
                <select aria-label="Barbeiro" value={funcionarioId} onChange={(e) => setFuncionarioId(e.target.value)}>
                  <option value="">Todos os barbeiros</option>
                  {barbeiros.dados?.map((barbeiro) => (
                    <option key={barbeiro.id} value={barbeiro.id}>
                      {barbeiro.nome}
                    </option>
                  ))}
                </select>
              </label>
            )}
          </div>
        </div>
      </div>

      <AvisoErro mensagem={erro ?? agenda.erro} />
      <AvisoSucesso mensagem={sucesso} />

      <div className="destaques">
        <div className="cartao destaque">
          <span>Agendamentos</span>
          <strong>{agendamentos.filter((item) => item.status !== 'CANCELADO').length}</strong>
        </div>
        <div className="cartao destaque">
          <span>Concluídos</span>
          <strong>{concluidos.length}</strong>
        </div>
        <div className="cartao destaque">
          <span>Total recebido</span>
          <strong>{formatarMoeda(totalDoDia)}</strong>
        </div>
      </div>

      <section className="cartao pilha">
        {agenda.carregando && <p className="texto-suave">Carregando...</p>}
        {!agenda.carregando && agendamentos.length === 0 && (
          <p className="texto-suave">Nenhum agendamento para este dia.</p>
        )}
        <ul className="lista">
          {agendamentos.map((agendamento) => {
            const horarioIniciado = new Date(agendamento.inicio).getTime() <= Date.now()
            return (
              <li key={agendamento.id} className="item-lista" style={{ borderBottom: '1px solid var(--cor-borda)', paddingBottom: '0.75rem' }}>
                <div className="linha" style={{ alignItems: 'center' }}>
                  <strong style={{ fontSize: '1.15rem', minWidth: 110 }}>
                    {formatarHora(agendamento.inicio)}–{formatarHora(agendamento.fim)}
                  </strong>
                  <div>
                    <div>
                      <strong>{agendamento.cliente.nome}</strong>{' '}
                      <span className="texto-suave">· {formatarTelefone(agendamento.cliente.telefone)}</span>
                    </div>
                    <div className="texto-suave">
                      {agendamento.itens.map((item) => item.nome).join(', ')}
                      {ehAdministrador && ` · ${agendamento.barbeiro.nome}`} ·{' '}
                      {formatarMoeda(agendamento.valorCobrado ?? agendamento.valorTabela)}
                      {agendamento.desconto ? ` (desconto de ${formatarMoeda(agendamento.desconto)})` : ''}
                    </div>
                  </div>
                </div>
                <div className="linha" style={{ alignItems: 'center' }}>
                  <span className={`etiqueta etiqueta-${agendamento.status}`}>
                    {NOME_STATUS_AGENDAMENTO[agendamento.status]}
                  </span>
                  {agendamento.status === 'AGENDADO' && (
                    <>
                      <button
                        type="button"
                        className="botao botao-pequeno"
                        onClick={() => setEmConclusao(agendamento)}
                        disabled={!horarioIniciado}
                        title={horarioIniciado ? undefined : 'Disponível a partir do horário do agendamento'}
                      >
                        Concluir
                      </button>
                      <button
                        type="button"
                        className="botao botao-secundario botao-pequeno"
                        onClick={() => executarAcao(agendamento, 'nao-comparecimento')}
                        disabled={!horarioIniciado}
                      >
                        Não compareceu
                      </button>
                      <button
                        type="button"
                        className="botao botao-perigo botao-pequeno"
                        onClick={() => executarAcao(agendamento, 'cancelamento')}
                      >
                        Cancelar
                      </button>
                    </>
                  )}
                </div>
              </li>
            )
          })}
        </ul>
      </section>

      {agendandoNoBalcao && (
        <ModalAgendamentoBalcao
          dataInicial={data < hojeIso() ? hojeIso() : data}
          ehAdministrador={ehAdministrador}
          aoFechar={() => setAgendandoNoBalcao(false)}
          aoAgendar={aoAgendarNoBalcao}
        />
      )}

      {emConclusao && (
        <ModalConclusaoAtendimento
          agendamento={emConclusao}
          aoFechar={() => setEmConclusao(null)}
          aoConcluir={aoConcluirAtendimento}
        />
      )}
    </div>
  )
}
