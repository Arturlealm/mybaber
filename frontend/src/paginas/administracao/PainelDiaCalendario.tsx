import { useState } from 'react'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type { CalendarioAgendaDia, TipoAjusteAgenda } from '../../api/tiposApi'
import { AvisoErro } from '../../componentes/AvisosOperacao'
import { formatarDataLonga, formatarHora } from '../../compartilhado/formatadores'
import { NOME_ORIGEM_EXPEDIENTE } from '../../compartilhado/nomesExibicao'

type Propriedades = {
  dia: CalendarioAgendaDia
  diaPassado: boolean
  aoAlterar: (mensagem: string) => void
}

type FormularioAjuste = {
  funcionarioId: number | null
  tipo: TipoAjusteAgenda
  horaInicio: string
  horaFim: string
  motivo: string
}

export function PainelDiaCalendario({ dia, diaPassado, aoAlterar }: Propriedades) {
  const [formulario, setFormulario] = useState<FormularioAjuste | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  function abrirFormulario(funcionarioId: number | null, tipo: TipoAjusteAgenda) {
    setErro(null)
    setFormulario({ funcionarioId, tipo, horaInicio: '09:00', horaFim: '18:00', motivo: '' })
  }

  async function salvar() {
    if (!formulario) return
    setErro(null)
    setEnviando(true)
    try {
      await requisitarApi('/api/agenda/ajustes', {
        metodo: 'POST',
        corpo: {
          data: dia.data,
          tipo: formulario.tipo,
          funcionarioId: formulario.funcionarioId,
          horaInicio: formulario.tipo === 'ABERTO' ? formulario.horaInicio : null,
          horaFim: formulario.tipo === 'ABERTO' ? formulario.horaFim : null,
          motivo: formulario.motivo || null,
        },
      })
      setFormulario(null)
      aoAlterar('Agenda atualizada.')
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  async function removerAjuste(ajusteId: number) {
    setErro(null)
    setEnviando(true)
    try {
      await requisitarApi(`/api/agenda/ajustes/${ajusteId}`, { metodo: 'DELETE' })
      aoAlterar('Ajuste removido. O dia volta a seguir o horário padrão.')
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  const nomeAlvo =
    formulario?.funcionarioId == null
      ? 'todos os barbeiros'
      : dia.funcionarios.find((item) => item.funcionarioId === formulario.funcionarioId)?.nome

  return (
    <section className="cartao pilha">
      <div>
        <h2 style={{ textTransform: 'capitalize' }}>{formatarDataLonga(dia.data)}</h2>
        {diaPassado && <p className="texto-suave">Datas passadas não podem ser alteradas.</p>}
      </div>

      <div className="pilha" style={{ borderBottom: '1px solid var(--cor-borda)', paddingBottom: '1rem' }}>
        <h3>Todos os barbeiros</h3>
        {dia.ajusteGeral ? (
          <p>
            <span className={`etiqueta ${dia.ajusteGeral.tipo === 'FECHADO' ? 'etiqueta-CANCELADO' : 'etiqueta-AGENDADO'}`}>
              {dia.ajusteGeral.tipo === 'FECHADO'
                ? 'Fechado para todos'
                : `Aberto para todos das ${formatarHora(dia.ajusteGeral.horaInicio!)} às ${formatarHora(dia.ajusteGeral.horaFim!)}`}
            </span>{' '}
            {dia.ajusteGeral.motivo && <span className="texto-suave">{dia.ajusteGeral.motivo}</span>}
          </p>
        ) : (
          <p className="texto-suave">Seguindo o horário padrão.</p>
        )}
        {!diaPassado && (
          <div className="linha">
            <button type="button" className="botao botao-perigo botao-pequeno" onClick={() => abrirFormulario(null, 'FECHADO')}>
              Fechar o dia para todos
            </button>
            <button type="button" className="botao botao-secundario botao-pequeno" onClick={() => abrirFormulario(null, 'ABERTO')}>
              Horário especial para todos
            </button>
            {dia.ajusteGeral && (
              <button
                type="button"
                className="botao botao-secundario botao-pequeno"
                onClick={() => removerAjuste(dia.ajusteGeral!.id)}
                disabled={enviando}
              >
                Voltar ao horário padrão
              </button>
            )}
          </div>
        )}
      </div>

      <div className="pilha">
        <h3>Por barbeiro</h3>
        {dia.funcionarios.length === 0 && <p className="texto-suave">Nenhum barbeiro cadastrado.</p>}
        <ul className="lista">
          {dia.funcionarios.map((funcionario) => (
            <li key={funcionario.funcionarioId} className="item-lista">
              <div>
                <strong>{funcionario.nome}</strong>{' '}
                <span className={`etiqueta ${funcionario.situacao === 'ABERTO' ? 'etiqueta-CONCLUIDO' : 'etiqueta-CANCELADO'}`}>
                  {funcionario.situacao === 'ABERTO'
                    ? funcionario.intervalos.map((intervalo) => `${formatarHora(intervalo.inicio)}–${formatarHora(intervalo.fim)}`).join(' e ')
                    : 'Fechado'}
                </span>
                <div className="campo-ajuda">
                  {NOME_ORIGEM_EXPEDIENTE[funcionario.origem]}
                  {funcionario.motivo && ` · ${funcionario.motivo}`}
                </div>
              </div>
              {!diaPassado && (
                <div className="linha">
                  {funcionario.situacao === 'ABERTO' ? (
                    <button
                      type="button"
                      className="botao botao-perigo botao-pequeno"
                      onClick={() => abrirFormulario(funcionario.funcionarioId, 'FECHADO')}
                    >
                      Fechar
                    </button>
                  ) : null}
                  <button
                    type="button"
                    className="botao botao-secundario botao-pequeno"
                    onClick={() => abrirFormulario(funcionario.funcionarioId, 'ABERTO')}
                  >
                    {funcionario.situacao === 'ABERTO' ? 'Alterar horário' : 'Abrir agenda'}
                  </button>
                  {funcionario.origem === 'AJUSTE_FUNCIONARIO' && funcionario.ajusteId && (
                    <button
                      type="button"
                      className="botao botao-secundario botao-pequeno"
                      onClick={() => removerAjuste(funcionario.ajusteId!)}
                      disabled={enviando}
                    >
                      Remover ajuste
                    </button>
                  )}
                </div>
              )}
            </li>
          ))}
        </ul>
      </div>

      {formulario && (
        <div className="pilha cartao" style={{ background: 'var(--cor-superficie-suave)' }}>
          <h3>
            {formulario.tipo === 'FECHADO' ? 'Fechar a agenda' : 'Abrir a agenda'} de {nomeAlvo}
          </h3>
          {formulario.tipo === 'ABERTO' && (
            <div className="linha">
              <label className="campo">
                Das
                <input
                  type="time"
                  step={1800}
                  value={formulario.horaInicio}
                  onChange={(e) => setFormulario({ ...formulario, horaInicio: e.target.value })}
                />
              </label>
              <label className="campo">
                Até
                <input
                  type="time"
                  step={1800}
                  value={formulario.horaFim}
                  onChange={(e) => setFormulario({ ...formulario, horaFim: e.target.value })}
                />
              </label>
            </div>
          )}
          <label className="campo">
            Motivo <span className="campo-ajuda">(opcional, ex.: Feriado)</span>
            <input
              value={formulario.motivo}
              onChange={(e) => setFormulario({ ...formulario, motivo: e.target.value })}
              maxLength={255}
            />
          </label>
          <AvisoErro mensagem={erro} />
          <div className="linha">
            <button type="button" className="botao" onClick={salvar} disabled={enviando}>
              {enviando ? 'Salvando...' : 'Salvar'}
            </button>
            <button type="button" className="botao botao-secundario" onClick={() => setFormulario(null)}>
              Cancelar
            </button>
          </div>
        </div>
      )}
      {!formulario && <AvisoErro mensagem={erro} />}
    </section>
  )
}
