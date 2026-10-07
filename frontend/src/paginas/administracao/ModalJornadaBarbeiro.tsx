import { useEffect, useState } from 'react'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type { Funcionario } from '../../api/tiposApi'
import { AvisoErro } from '../../componentes/AvisosOperacao'
import {
  converterIntervalosParaJornada,
  converterJornadaParaIntervalos,
  EditorJornadaSemanal,
  type IntervaloJornada,
  type JornadaSemanalEditavel,
} from '../../componentes/EditorJornadaSemanal'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'

type JornadaBarbeiro = { funcionarioId: number; segueJornadaDaFilial: boolean; intervalos: IntervaloJornada[] }
type JornadaFilial = { filialId: number; intervalos: IntervaloJornada[] }

type Propriedades = {
  funcionario: Funcionario
  aoFechar: () => void
  aoSalvar: (mensagem: string) => void
}

export function ModalJornadaBarbeiro({ funcionario, aoFechar, aoSalvar }: Propriedades) {
  const jornadaBarbeiro = useConsultaApi(
    () => requisitarApi<JornadaBarbeiro>(`/api/agenda/jornadas/funcionarios/${funcionario.id}`),
    [funcionario.id],
  )
  const jornadaFilial = useConsultaApi(
    () => requisitarApi<JornadaFilial>(`/api/agenda/jornadas/filiais/${funcionario.filialId}`),
    [funcionario.filialId],
  )
  const [segueJornadaDaFilial, setSegueJornadaDaFilial] = useState(true)
  const [horarios, setHorarios] = useState<JornadaSemanalEditavel | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    if (!jornadaBarbeiro.dados || !jornadaFilial.dados) return
    setSegueJornadaDaFilial(jornadaBarbeiro.dados.segueJornadaDaFilial)
    setHorarios(
      converterIntervalosParaJornada(
        jornadaBarbeiro.dados.segueJornadaDaFilial ? jornadaFilial.dados.intervalos : jornadaBarbeiro.dados.intervalos,
      ),
    )
  }, [jornadaBarbeiro.dados, jornadaFilial.dados])

  async function salvar() {
    if (!horarios) return
    setErro(null)
    setEnviando(true)
    try {
      await requisitarApi(`/api/agenda/jornadas/funcionarios/${funcionario.id}`, {
        metodo: 'PUT',
        corpo: { intervalos: segueJornadaDaFilial ? [] : converterJornadaParaIntervalos(horarios) },
      })
      aoSalvar(`Horário de ${funcionario.nome} atualizado.`)
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="fundo-modal" role="dialog" aria-modal="true" aria-labelledby="titulo-jornada-barbeiro">
      <div className="cartao modal pilha" style={{ maxWidth: 760 }}>
        <h2 id="titulo-jornada-barbeiro">Horário de {funcionario.nome}</h2>
        <AvisoErro mensagem={jornadaBarbeiro.erro ?? jornadaFilial.erro} />

        <label className="linha" style={{ alignItems: 'center' }}>
          <input
            type="radio"
            name="tipo-jornada"
            checked={segueJornadaDaFilial}
            onChange={() => setSegueJornadaDaFilial(true)}
          />
          Seguir o horário padrão da barbearia
        </label>
        <label className="linha" style={{ alignItems: 'center' }}>
          <input
            type="radio"
            name="tipo-jornada"
            checked={!segueJornadaDaFilial}
            onChange={() => setSegueJornadaDaFilial(false)}
          />
          Usar um horário próprio
        </label>

        {horarios && !segueJornadaDaFilial && <EditorJornadaSemanal jornada={horarios} aoAlterar={setHorarios} />}
        <p className="campo-ajuda">
          Para folgas ou feriados em datas específicas, use o Calendário.
        </p>

        <AvisoErro mensagem={erro} />
        <div className="linha">
          <button type="button" className="botao" onClick={salvar} disabled={enviando || !horarios}>
            {enviando ? 'Salvando...' : 'Salvar'}
          </button>
          <button type="button" className="botao botao-secundario" onClick={aoFechar} disabled={enviando}>
            Cancelar
          </button>
        </div>
      </div>
    </div>
  )
}
