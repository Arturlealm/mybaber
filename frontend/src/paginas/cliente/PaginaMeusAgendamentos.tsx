import { useState } from 'react'
import { Link } from 'react-router-dom'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type { Agendamento, Pagina } from '../../api/tiposApi'
import { AvisoErro, AvisoSucesso } from '../../componentes/AvisosOperacao'
import { NOME_STATUS_AGENDAMENTO } from '../../compartilhado/nomesExibicao'
import { formatarDataLonga, formatarHora, formatarMoeda } from '../../compartilhado/formatadores'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'

export function PaginaMeusAgendamentos() {
  const [erro, setErro] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)
  const [cancelandoId, setCancelandoId] = useState<number | null>(null)
  const agendamentos = useConsultaApi(
    () => requisitarApi<Pagina<Agendamento>>('/api/agendamentos/me', { parametros: { size: 50 } }),
    [],
  )

  const lista = agendamentos.dados?.conteudo ?? []
  const proximos = lista.filter((item) => item.status === 'AGENDADO').reverse()
  const historico = lista.filter((item) => item.status !== 'AGENDADO')

  async function cancelar(agendamento: Agendamento) {
    if (!window.confirm(`Cancelar o agendamento de ${formatarDataLonga(agendamento.inicio)} às ${formatarHora(agendamento.inicio)}?`)) {
      return
    }
    setErro(null)
    setSucesso(null)
    setCancelandoId(agendamento.id)
    try {
      await requisitarApi(`/api/agendamentos/${agendamento.id}/cancelamento`, { metodo: 'PATCH', corpo: {} })
      setSucesso('Agendamento cancelado.')
      agendamentos.recarregar()
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setCancelandoId(null)
    }
  }

  return (
    <div className="pilha">
      <div className="titulo-pagina">
        <div>
          <h1>Meus agendamentos</h1>
          <p>Seus próximos horários e o histórico de atendimentos.</p>
        </div>
        <Link to="/agendar" className="botao">
          Novo agendamento
        </Link>
      </div>

      <AvisoErro mensagem={erro ?? agendamentos.erro} />
      <AvisoSucesso mensagem={sucesso} />

      <section className="cartao pilha">
        <h2>Próximos</h2>
        {agendamentos.carregando && <p className="texto-suave">Carregando...</p>}
        {!agendamentos.carregando && proximos.length === 0 && (
          <p className="texto-suave">Você não tem horários marcados.</p>
        )}
        <ul className="lista">
          {proximos.map((agendamento) => (
            <li key={agendamento.id} className="item-lista">
              <div>
                <strong>
                  {formatarDataLonga(agendamento.inicio)} · {formatarHora(agendamento.inicio)}
                </strong>
                <div className="texto-suave">
                  {agendamento.itens.map((item) => item.nome).join(', ')} com {agendamento.barbeiro.nome} ·{' '}
                  {formatarMoeda(agendamento.valorTabela)}
                </div>
              </div>
              <button
                type="button"
                className="botao botao-perigo botao-pequeno"
                onClick={() => cancelar(agendamento)}
                disabled={cancelandoId === agendamento.id}
              >
                Cancelar
              </button>
            </li>
          ))}
        </ul>
        <p className="campo-ajuda">Cancelamentos pelo sistema podem ser feitos até 2 horas antes do horário.</p>
      </section>

      <section className="cartao pilha">
        <h2>Histórico</h2>
        {historico.length === 0 && <p className="texto-suave">Nenhum atendimento anterior.</p>}
        <ul className="lista">
          {historico.map((agendamento) => (
            <li key={agendamento.id} className="item-lista">
              <div>
                <strong>
                  {formatarDataLonga(agendamento.inicio)} · {formatarHora(agendamento.inicio)}
                </strong>
                <div className="texto-suave">
                  {agendamento.itens.map((item) => item.nome).join(', ')} com {agendamento.barbeiro.nome}
                  {agendamento.valorCobrado !== null && ` · ${formatarMoeda(agendamento.valorCobrado)}`}
                </div>
              </div>
              <span className={`etiqueta etiqueta-${agendamento.status}`}>
                {NOME_STATUS_AGENDAMENTO[agendamento.status]}
              </span>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
