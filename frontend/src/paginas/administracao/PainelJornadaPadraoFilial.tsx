import { useEffect, useState } from 'react'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type { DiaSemana } from '../../api/tiposApi'
import { AvisoErro, AvisoSucesso } from '../../componentes/AvisosOperacao'
import { formatarHora } from '../../compartilhado/formatadores'
import { NOME_DIA_SEMANA } from '../../compartilhado/nomesExibicao'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'

type JornadaFilial = {
  filialId: number
  intervalos: { diaSemana: DiaSemana; horaInicio: string; horaFim: string }[]
}

type HorarioDia = { aberto: boolean; horaInicio: string; horaFim: string }

const DIAS_SEMANA: DiaSemana[] = ['SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO', 'DOMINGO']

export function PainelJornadaPadraoFilial({ filialId, aoAlterar }: { filialId: number; aoAlterar: () => void }) {
  const jornada = useConsultaApi(
    () => requisitarApi<JornadaFilial>(`/api/agenda/jornadas/filiais/${filialId}`),
    [filialId],
  )
  const [editando, setEditando] = useState(false)
  const [horarios, setHorarios] = useState<Record<DiaSemana, HorarioDia> | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)

  useEffect(() => {
    if (!jornada.dados) return
    const porDia = {} as Record<DiaSemana, HorarioDia>
    DIAS_SEMANA.forEach((dia) => {
      const intervalo = jornada.dados!.intervalos.find((item) => item.diaSemana === dia)
      porDia[dia] = intervalo
        ? { aberto: true, horaInicio: formatarHora(intervalo.horaInicio), horaFim: formatarHora(intervalo.horaFim) }
        : { aberto: false, horaInicio: '09:00', horaFim: '18:00' }
    })
    setHorarios(porDia)
  }, [jornada.dados])

  function alterar(dia: DiaSemana, alteracao: Partial<HorarioDia>) {
    setHorarios((atual) => (atual ? { ...atual, [dia]: { ...atual[dia], ...alteracao } } : atual))
  }

  async function salvar() {
    if (!horarios) return
    setErro(null)
    setSucesso(null)
    try {
      await requisitarApi(`/api/agenda/jornadas/filiais/${filialId}`, {
        metodo: 'PUT',
        corpo: {
          intervalos: DIAS_SEMANA.filter((dia) => horarios[dia].aberto).map((dia) => ({
            diaSemana: dia,
            horaInicio: horarios[dia].horaInicio,
            horaFim: horarios[dia].horaFim,
          })),
        },
      })
      setSucesso('Horário padrão atualizado.')
      setEditando(false)
      jornada.recarregar()
      aoAlterar()
    } catch (falha) {
      setErro(descreverErro(falha))
    }
  }

  return (
    <section className="cartao pilha">
      <div className="item-lista">
        <div>
          <h2>Horário padrão da barbearia</h2>
          <p className="texto-suave">Vale para os barbeiros que não têm horário próprio.</p>
        </div>
        {!editando && (
          <button type="button" className="botao botao-secundario botao-pequeno" onClick={() => setEditando(true)}>
            Editar
          </button>
        )}
      </div>
      <AvisoErro mensagem={erro ?? jornada.erro} />
      <AvisoSucesso mensagem={sucesso} />
      {horarios && (
        <div className="tabela-container">
          <table>
            <tbody>
              {DIAS_SEMANA.map((dia) => (
                <tr key={dia}>
                  <td>{NOME_DIA_SEMANA[dia]}</td>
                  {editando ? (
                    <>
                      <td>
                        <label className="linha" style={{ alignItems: 'center' }}>
                          <input
                            type="checkbox"
                            checked={horarios[dia].aberto}
                            onChange={(e) => alterar(dia, { aberto: e.target.checked })}
                          />
                          Aberto
                        </label>
                      </td>
                      <td>
                        <input
                          type="time"
                          step={1800}
                          value={horarios[dia].horaInicio}
                          disabled={!horarios[dia].aberto}
                          onChange={(e) => alterar(dia, { horaInicio: e.target.value })}
                        />
                      </td>
                      <td>
                        <input
                          type="time"
                          step={1800}
                          value={horarios[dia].horaFim}
                          disabled={!horarios[dia].aberto}
                          onChange={(e) => alterar(dia, { horaFim: e.target.value })}
                        />
                      </td>
                    </>
                  ) : (
                    <td className="texto-suave">
                      {horarios[dia].aberto ? `${horarios[dia].horaInicio} às ${horarios[dia].horaFim}` : 'Fechado'}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {editando && (
        <div className="linha">
          <button type="button" className="botao" onClick={salvar}>
            Salvar horário padrão
          </button>
          <button
            type="button"
            className="botao botao-secundario"
            onClick={() => {
              setEditando(false)
              jornada.recarregar()
            }}
          >
            Cancelar
          </button>
        </div>
      )}
    </section>
  )
}
