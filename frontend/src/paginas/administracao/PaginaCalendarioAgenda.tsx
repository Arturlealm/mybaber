import { useMemo, useState } from 'react'

import { requisitarApi } from '../../api/clienteHttp'
import type { CalendarioAgendaDia } from '../../api/tiposApi'
import { AvisoErro, AvisoSucesso } from '../../componentes/AvisosOperacao'
import { formatarDataIso, formatarMesAno, hojeIso } from '../../compartilhado/formatadores'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'
import { PainelDiaCalendario } from './PainelDiaCalendario'
import { PainelJornadaPadraoFilial } from './PainelJornadaPadraoFilial'

const NOMES_DIAS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']

type Filial = { id: number; nome: string }

function resumirDia(dia: CalendarioAgendaDia) {
  const abertos = dia.funcionarios.filter((funcionario) => funcionario.situacao === 'ABERTO').length
  const temAjusteIndividual = dia.funcionarios.some((funcionario) => funcionario.origem === 'AJUSTE_FUNCIONARIO')
  return { abertos, total: dia.funcionarios.length, temAjusteIndividual }
}

export function PaginaCalendarioAgenda() {
  const hoje = hojeIso()
  const [mesExibido, setMesExibido] = useState(() => {
    const agora = new Date()
    return new Date(agora.getFullYear(), agora.getMonth(), 1)
  })
  const [dataSelecionada, setDataSelecionada] = useState<string>(hoje)
  const [mensagem, setMensagem] = useState<string | null>(null)

  const inicioMes = formatarDataIso(mesExibido)
  const fimMes = formatarDataIso(new Date(mesExibido.getFullYear(), mesExibido.getMonth() + 1, 0))

  const filiais = useConsultaApi(() => requisitarApi<Filial[]>('/api/filiais'), [])
  const calendario = useConsultaApi(
    () => requisitarApi<CalendarioAgendaDia[]>('/api/agenda/calendario', { parametros: { inicio: inicioMes, fim: fimMes } }),
    [inicioMes, fimMes],
  )

  const espacosAntesDoPrimeiroDia = mesExibido.getDay()
  const diaSelecionado = calendario.dados?.find((dia) => dia.data === dataSelecionada) ?? null

  const celulas = useMemo(() => calendario.dados ?? [], [calendario.dados])

  function mudarMes(deslocamento: number) {
    const novoMes = new Date(mesExibido.getFullYear(), mesExibido.getMonth() + deslocamento, 1)
    setMesExibido(novoMes)
    setDataSelecionada(formatarDataIso(novoMes))
  }

  function aoAlterarAgenda(texto: string) {
    setMensagem(texto)
    calendario.recarregar()
  }

  return (
    <div className="pilha">
      <div className="titulo-pagina">
        <div>
          <h1>Calendário da agenda</h1>
          <p>Feche dias (como feriados) para todos ou abra a agenda de um barbeiro em um dia específico.</p>
        </div>
        <div className="linha" style={{ alignItems: 'center' }}>
          <button type="button" className="botao botao-secundario" onClick={() => mudarMes(-1)}>
            ‹
          </button>
          <strong style={{ minWidth: 150, textAlign: 'center' }}>
            {formatarMesAno(mesExibido)}
          </strong>
          <button type="button" className="botao botao-secundario" onClick={() => mudarMes(1)}>
            ›
          </button>
        </div>
      </div>

      <AvisoErro mensagem={calendario.erro} />
      <AvisoSucesso mensagem={mensagem} />

      <div className="layout-calendario">
        <section className="cartao">
          <div className="calendario">
            {NOMES_DIAS.map((nome) => (
              <div key={nome} className="calendario-cabecalho">
                {nome}
              </div>
            ))}
            {Array.from({ length: espacosAntesDoPrimeiroDia }, (_, indice) => (
              <div key={`vazio-${indice}`} />
            ))}
            {celulas.map((dia) => {
              const resumo = resumirDia(dia)
              const fechado = resumo.abertos === 0
              const classes = ['dia-calendario', fechado ? 'fechado' : '', dia.data < hoje ? 'passado' : '']
              return (
                <button
                  key={dia.data}
                  type="button"
                  className={classes.join(' ')}
                  aria-pressed={dia.data === dataSelecionada}
                  onClick={() => {
                    setMensagem(null)
                    setDataSelecionada(dia.data)
                  }}
                >
                  <span className="dia-numero">{Number(dia.data.slice(8, 10))}</span>
                  {dia.ajusteGeral?.tipo === 'FECHADO' ? (
                    <span className="dia-resumo alerta">{dia.ajusteGeral.motivo || 'Fechado'}</span>
                  ) : (
                    <span className="dia-resumo">
                      {fechado ? 'Fechado' : `${resumo.abertos}/${resumo.total} abertos`}
                    </span>
                  )}
                  {resumo.temAjusteIndividual && <span className="dia-resumo especial">Ajuste individual</span>}
                </button>
              )
            })}
          </div>
        </section>

        <div className="pilha">
          {diaSelecionado && (
            <PainelDiaCalendario
              key={diaSelecionado.data}
              dia={diaSelecionado}
              diaPassado={diaSelecionado.data < hoje}
              aoAlterar={aoAlterarAgenda}
            />
          )}
          {filiais.dados?.[0] && (
            <PainelJornadaPadraoFilial filialId={filiais.dados[0].id} aoAlterar={calendario.recarregar} />
          )}
        </div>
      </div>
    </div>
  )
}
