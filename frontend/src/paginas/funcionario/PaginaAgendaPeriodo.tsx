import { useState } from 'react'
import { Link } from 'react-router-dom'

import { requisitarApi } from '../../api/clienteHttp'
import type { Agendamento, BarbeiroResumo } from '../../api/tiposApi'
import { useAutenticacao } from '../../autenticacao/ContextoAutenticacao'
import { AvisoErro } from '../../componentes/AvisosOperacao'
import {
  capitalizar,
  converterDataIso,
  formatarDataCurta,
  formatarDataLonga,
  formatarDataIso,
  formatarHora,
  formatarMesAno,
  hojeIso,
  somarDias,
} from '../../compartilhado/formatadores'
import { NOME_STATUS_AGENDAMENTO } from '../../compartilhado/nomesExibicao'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'

type TipoPeriodo = 'semana' | 'mes'

const CHAVE_PERIODO_PREFERIDO = 'mybarber.agenda.periodo'

function lerPeriodoPreferido(): TipoPeriodo {
  try {
    return localStorage.getItem(CHAVE_PERIODO_PREFERIDO) === 'mes' ? 'mes' : 'semana'
  } catch {
    return 'semana'
  }
}

function calcularPeriodo(tipo: TipoPeriodo, referencia: string) {
  const data = converterDataIso(referencia)
  if (tipo === 'mes') {
    return {
      inicio: formatarDataIso(new Date(data.getFullYear(), data.getMonth(), 1)),
      fim: formatarDataIso(new Date(data.getFullYear(), data.getMonth() + 1, 0)),
    }
  }
  const diasDesdeSegunda = (data.getDay() + 6) % 7
  const inicio = somarDias(referencia, -diasDesdeSegunda)
  return { inicio, fim: somarDias(inicio, 6) }
}

function descreverDia(dataIso: string) {
  const hoje = hojeIso()
  const prefixo = dataIso === hoje ? 'Hoje · ' : dataIso === somarDias(hoje, 1) ? 'Amanhã · ' : ''
  return prefixo + capitalizar(formatarDataLonga(dataIso))
}

export function PaginaAgendaPeriodo() {
  const { sessao } = useAutenticacao()
  const ehAdministrador = sessao?.perfil === 'ADMINISTRADOR'
  const [tipoPeriodo, setTipoPeriodo] = useState<TipoPeriodo>(lerPeriodoPreferido)
  const [referencia, setReferencia] = useState(hojeIso())
  const [funcionarioId, setFuncionarioId] = useState('')
  const [mostrarCancelados, setMostrarCancelados] = useState(false)

  const { inicio, fim } = calcularPeriodo(tipoPeriodo, referencia)

  const barbeiros = useConsultaApi(
    ehAdministrador ? () => requisitarApi<BarbeiroResumo[]>('/api/funcionarios/barbeiros') : null,
    [ehAdministrador],
  )
  const agenda = useConsultaApi(
    () =>
      requisitarApi<Agendamento[]>('/api/agendamentos/periodo', {
        parametros: { inicio, fim, funcionarioId: funcionarioId || undefined },
      }),
    [inicio, fim, funcionarioId],
  )

  const agendamentos = (agenda.dados ?? []).filter(
    (agendamento) => mostrarCancelados || agendamento.status !== 'CANCELADO',
  )
  const agendamentosPorDia = agendamentos.reduce<Record<string, Agendamento[]>>((grupos, agendamento) => {
    const dia = agendamento.inicio.slice(0, 10)
    grupos[dia] = [...(grupos[dia] ?? []), agendamento]
    return grupos
  }, {})
  const dias = Object.keys(agendamentosPorDia).sort()
  const quantidadeAgendados = agendamentos.filter((agendamento) => agendamento.status === 'AGENDADO').length
  const quantidadeConcluidos = agendamentos.filter((agendamento) => agendamento.status === 'CONCLUIDO').length

  function escolherPeriodo(tipo: TipoPeriodo) {
    setTipoPeriodo(tipo)
    try {
      localStorage.setItem(CHAVE_PERIODO_PREFERIDO, tipo)
    } catch {
      /* A preferência vale apenas enquanto a página estiver aberta */
    }
  }

  function navegar(direcao: -1 | 1) {
    if (tipoPeriodo === 'semana') {
      setReferencia(somarDias(referencia, 7 * direcao))
      return
    }
    const data = converterDataIso(referencia)
    setReferencia(formatarDataIso(new Date(data.getFullYear(), data.getMonth() + direcao, 1)))
  }

  const tituloPeriodo =
    tipoPeriodo === 'mes'
      ? formatarMesAno(converterDataIso(inicio))
      : `${formatarDataCurta(inicio).slice(0, 5)} a ${formatarDataCurta(fim).slice(0, 5)}`

  return (
    <div className="pilha">
      <div className="titulo-pagina">
        <div>
          <h1>{ehAdministrador ? 'Agendamentos' : 'Meus agendamentos'}</h1>
          <p>{tituloPeriodo}</p>
        </div>
        <div className="barra-ferramentas">
          <div className="abas" role="tablist" style={{ margin: 0, minWidth: 220 }}>
            <button
              type="button"
              role="tab"
              className="aba"
              aria-selected={tipoPeriodo === 'semana'}
              onClick={() => escolherPeriodo('semana')}
            >
              Semana
            </button>
            <button
              type="button"
              role="tab"
              className="aba"
              aria-selected={tipoPeriodo === 'mes'}
              onClick={() => escolherPeriodo('mes')}
            >
              Mês
            </button>
          </div>
          <div className="grupo-ferramentas">
            <button type="button" className="botao botao-secundario" onClick={() => navegar(-1)} aria-label="Período anterior">
              ‹
            </button>
            <button type="button" className="botao botao-secundario" onClick={() => setReferencia(hojeIso())}>
              Atual
            </button>
            <button type="button" className="botao botao-secundario" onClick={() => navegar(1)} aria-label="Próximo período">
              ›
            </button>
          </div>
          {ehAdministrador && (
            <div className="grupo-ferramentas">
              <label className="campo">
                <select value={funcionarioId} onChange={(e) => setFuncionarioId(e.target.value)}>
                  <option value="">Todos os barbeiros</option>
                  {barbeiros.dados?.map((barbeiro) => (
                    <option key={barbeiro.id} value={barbeiro.id}>
                      {barbeiro.nome}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          )}
        </div>
      </div>

      <AvisoErro mensagem={agenda.erro} />

      <div className="destaques">
        <div className="cartao destaque">
          <span>A atender</span>
          <strong>{quantidadeAgendados}</strong>
        </div>
        <div className="cartao destaque">
          <span>Concluídos</span>
          <strong>{quantidadeConcluidos}</strong>
        </div>
      </div>

      <section className="cartao pilha">
        <label className="opcao-marcavel" style={{ fontSize: '0.9rem' }}>
          <input type="checkbox" checked={mostrarCancelados} onChange={(e) => setMostrarCancelados(e.target.checked)} />
          Mostrar cancelados
        </label>

        {agenda.carregando && <p className="texto-suave">Carregando...</p>}
        {!agenda.carregando && dias.length === 0 && <p className="texto-suave">Nenhum agendamento neste período.</p>}

        {dias.map((dia) => (
          <div key={dia} className={`grupo-dia ${dia < hojeIso() ? 'passado' : ''}`}>
            <div className="cabecalho-dia">
              <h3 style={{ margin: 0 }}>{descreverDia(dia)}</h3>
              <Link to={`/agenda-do-dia?data=${dia}`} className="campo-ajuda">
                Abrir dia
              </Link>
            </div>
            {agendamentosPorDia[dia].map((agendamento) => (
              <div key={agendamento.id} className="item-agenda">
                <span className="horario-agenda">
                  {formatarHora(agendamento.inicio)}–{formatarHora(agendamento.fim)}
                </span>
                <span className="detalhes-agenda">
                  <strong>{agendamento.cliente.nome}</strong>{' '}
                  <span className="texto-suave">
                    · {agendamento.itens.map((item) => item.nome).join(', ')}
                    {ehAdministrador && ` · ${agendamento.barbeiro.nome}`}
                  </span>
                </span>
                <span className={`etiqueta etiqueta-${agendamento.status}`}>
                  {NOME_STATUS_AGENDAMENTO[agendamento.status]}
                </span>
              </div>
            ))}
          </div>
        ))}
      </section>
    </div>
  )
}
