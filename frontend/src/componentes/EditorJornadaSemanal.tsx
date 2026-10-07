import type { DiaSemana } from '../api/tiposApi'
import { formatarHora } from '../compartilhado/formatadores'
import { NOME_DIA_SEMANA } from '../compartilhado/nomesExibicao'

export type IntervaloJornada = { diaSemana: DiaSemana; horaInicio: string; horaFim: string }

export type HorarioDiaJornada = {
  aberto: boolean
  abertura: string
  fechamento: string
  temPausa: boolean
  inicioPausa: string
  fimPausa: string
}

export type JornadaSemanalEditavel = Record<DiaSemana, HorarioDiaJornada>

export const DIAS_SEMANA: DiaSemana[] = ['SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO', 'DOMINGO']

const DIA_FECHADO: HorarioDiaJornada = {
  aberto: false,
  abertura: '09:00',
  fechamento: '18:00',
  temPausa: false,
  inicioPausa: '12:00',
  fimPausa: '13:00',
}

export function converterIntervalosParaJornada(intervalos: IntervaloJornada[]): JornadaSemanalEditavel {
  const jornada = {} as JornadaSemanalEditavel
  DIAS_SEMANA.forEach((dia) => {
    const doDia = intervalos
      .filter((intervalo) => intervalo.diaSemana === dia)
      .map((intervalo) => ({ inicio: formatarHora(intervalo.horaInicio), fim: formatarHora(intervalo.horaFim) }))
      .sort((primeiro, segundo) => primeiro.inicio.localeCompare(segundo.inicio))

    if (doDia.length === 0) {
      jornada[dia] = { ...DIA_FECHADO }
      return
    }
    jornada[dia] = {
      aberto: true,
      abertura: doDia[0].inicio,
      fechamento: doDia[doDia.length - 1].fim,
      temPausa: doDia.length > 1,
      inicioPausa: doDia.length > 1 ? doDia[0].fim : DIA_FECHADO.inicioPausa,
      fimPausa: doDia.length > 1 ? doDia[1].inicio : DIA_FECHADO.fimPausa,
    }
  })
  return jornada
}

export function converterJornadaParaIntervalos(jornada: JornadaSemanalEditavel): IntervaloJornada[] {
  return DIAS_SEMANA.filter((dia) => jornada[dia].aberto).flatMap((dia) => {
    const horario = jornada[dia]
    if (!horario.temPausa) {
      return [{ diaSemana: dia, horaInicio: horario.abertura, horaFim: horario.fechamento }]
    }
    return [
      { diaSemana: dia, horaInicio: horario.abertura, horaFim: horario.inicioPausa },
      { diaSemana: dia, horaInicio: horario.fimPausa, horaFim: horario.fechamento },
    ]
  })
}

export function descreverHorarioDia(horario: HorarioDiaJornada) {
  if (!horario.aberto) return 'Fechado'
  const pausa = horario.temPausa ? ` (pausa ${horario.inicioPausa}–${horario.fimPausa})` : ''
  return `${horario.abertura} às ${horario.fechamento}${pausa}`
}

type Propriedades = {
  jornada: JornadaSemanalEditavel
  aoAlterar: (jornada: JornadaSemanalEditavel) => void
}

export function EditorJornadaSemanal({ jornada, aoAlterar }: Propriedades) {
  function alterar(dia: DiaSemana, alteracao: Partial<HorarioDiaJornada>) {
    aoAlterar({ ...jornada, [dia]: { ...jornada[dia], ...alteracao } })
  }

  return (
    <div className="tabela-container">
      <table>
        <thead>
          <tr>
            <th>Dia</th>
            <th>Aberto</th>
            <th>Abre</th>
            <th>Fecha</th>
            <th>Pausa (almoço)</th>
          </tr>
        </thead>
        <tbody>
          {DIAS_SEMANA.map((dia) => {
            const horario = jornada[dia]
            return (
              <tr key={dia}>
                <td>{NOME_DIA_SEMANA[dia]}</td>
                <td>
                  <input
                    type="checkbox"
                    aria-label={`${NOME_DIA_SEMANA[dia]} aberto`}
                    checked={horario.aberto}
                    onChange={(e) => alterar(dia, { aberto: e.target.checked })}
                  />
                </td>
                <td>
                  <input
                    type="time"
                    step={1800}
                    value={horario.abertura}
                    disabled={!horario.aberto}
                    onChange={(e) => alterar(dia, { abertura: e.target.value })}
                  />
                </td>
                <td>
                  <input
                    type="time"
                    step={1800}
                    value={horario.fechamento}
                    disabled={!horario.aberto}
                    onChange={(e) => alterar(dia, { fechamento: e.target.value })}
                  />
                </td>
                <td>
                  <div className="linha" style={{ alignItems: 'center', flexWrap: 'nowrap' }}>
                    <input
                      type="checkbox"
                      aria-label={`${NOME_DIA_SEMANA[dia]} com pausa`}
                      checked={horario.temPausa}
                      disabled={!horario.aberto}
                      onChange={(e) => alterar(dia, { temPausa: e.target.checked })}
                    />
                    <input
                      type="time"
                      step={1800}
                      value={horario.inicioPausa}
                      disabled={!horario.aberto || !horario.temPausa}
                      onChange={(e) => alterar(dia, { inicioPausa: e.target.value })}
                    />
                    <span className="texto-suave">às</span>
                    <input
                      type="time"
                      step={1800}
                      value={horario.fimPausa}
                      disabled={!horario.aberto || !horario.temPausa}
                      onChange={(e) => alterar(dia, { fimPausa: e.target.value })}
                    />
                  </div>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
