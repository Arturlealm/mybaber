import { useEffect, useState } from 'react'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import { AvisoErro, AvisoSucesso } from '../../componentes/AvisosOperacao'
import {
  converterIntervalosParaJornada,
  converterJornadaParaIntervalos,
  descreverHorarioDia,
  DIAS_SEMANA,
  EditorJornadaSemanal,
  type IntervaloJornada,
  type JornadaSemanalEditavel,
} from '../../componentes/EditorJornadaSemanal'
import { NOME_DIA_SEMANA } from '../../compartilhado/nomesExibicao'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'

type JornadaFilial = { filialId: number; intervalos: IntervaloJornada[] }

export function PainelJornadaPadraoFilial({ filialId, aoAlterar }: { filialId: number; aoAlterar: () => void }) {
  const jornada = useConsultaApi(
    () => requisitarApi<JornadaFilial>(`/api/agenda/jornadas/filiais/${filialId}`),
    [filialId],
  )
  const [editando, setEditando] = useState(false)
  const [horarios, setHorarios] = useState<JornadaSemanalEditavel | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)

  useEffect(() => {
    if (jornada.dados) setHorarios(converterIntervalosParaJornada(jornada.dados.intervalos))
  }, [jornada.dados])

  async function salvar() {
    if (!horarios) return
    setErro(null)
    setSucesso(null)
    try {
      await requisitarApi(`/api/agenda/jornadas/filiais/${filialId}`, {
        metodo: 'PUT',
        corpo: { intervalos: converterJornadaParaIntervalos(horarios) },
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
      {horarios && editando && <EditorJornadaSemanal jornada={horarios} aoAlterar={setHorarios} />}
      {horarios && !editando && (
        <div className="tabela-container">
          <table>
            <tbody>
              {DIAS_SEMANA.map((dia) => (
                <tr key={dia}>
                  <td>{NOME_DIA_SEMANA[dia]}</td>
                  <td className="texto-suave">{descreverHorarioDia(horarios[dia])}</td>
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
