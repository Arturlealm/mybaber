import { useState } from 'react'

import { requisitarApi } from '../../api/clienteHttp'
import type { RelatorioDesempenhoBarbeiros } from '../../api/tiposApi'
import { AvisoErro } from '../../componentes/AvisosOperacao'
import { formatarDataIso, formatarMoeda, hojeIso } from '../../compartilhado/formatadores'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'

function primeiroDiaDoMes() {
  const hoje = new Date()
  return formatarDataIso(new Date(hoje.getFullYear(), hoje.getMonth(), 1))
}

export function PaginaRelatorios() {
  const [inicio, setInicio] = useState(primeiroDiaDoMes())
  const [fim, setFim] = useState(hojeIso())
  const relatorio = useConsultaApi(
    inicio && fim
      ? () =>
          requisitarApi<RelatorioDesempenhoBarbeiros>('/api/relatorios/desempenho-barbeiros', {
            parametros: { inicio, fim },
          })
      : null,
    [inicio, fim],
  )
  const dados = relatorio.dados
  const nomesServicos = Array.from(
    new Set(dados?.barbeiros.flatMap((barbeiro) => barbeiro.servicos.map((servico) => servico.nome)) ?? []),
  )

  return (
    <div className="pilha">
      <div className="titulo-pagina">
        <div>
          <h1>Relatórios</h1>
          <p>Desempenho dos barbeiros com base nos atendimentos concluídos e no valor cobrado.</p>
        </div>
        <div className="linha">
          <label className="campo">
            De
            <input type="date" value={inicio} onChange={(e) => setInicio(e.target.value)} />
          </label>
          <label className="campo">
            Até
            <input type="date" value={fim} onChange={(e) => setFim(e.target.value)} />
          </label>
        </div>
      </div>

      <AvisoErro mensagem={relatorio.erro} />
      {relatorio.carregando && <p className="texto-suave">Carregando...</p>}

      {dados && (
        <>
          <div className="destaques">
            <div className="cartao destaque">
              <span>Faturamento</span>
              <strong>{formatarMoeda(dados.totais.faturamento)}</strong>
              <span>{formatarMoeda(dados.totais.descontos)} em descontos</span>
            </div>
            <div className="cartao destaque">
              <span>Atendimentos concluídos</span>
              <strong>{dados.totais.atendimentosConcluidos}</strong>
            </div>
            <div className="cartao destaque">
              <span>Quem mais faturou</span>
              <strong>{dados.maiorFaturamento?.nomeFuncionario ?? '—'}</strong>
              {dados.maiorFaturamento && <span>{formatarMoeda(dados.maiorFaturamento.valor)}</span>}
            </div>
            <div className="cartao destaque">
              <span>Quem mais atendeu</span>
              <strong>{dados.maisAtendimentos?.nomeFuncionario ?? '—'}</strong>
              {dados.maisAtendimentos && <span>{dados.maisAtendimentos.quantidade} atendimentos</span>}
            </div>
            {dados.maisAtendimentosPorServico.map((destaque) => (
              <div key={destaque.servicoId} className="cartao destaque">
                <span>Mais {destaque.nomeServico?.toLowerCase()}</span>
                <strong>{destaque.nomeFuncionario}</strong>
                <span>{destaque.quantidade} no período</span>
              </div>
            ))}
          </div>

          <section className="cartao pilha">
            <h2>Por barbeiro</h2>
            {dados.barbeiros.length === 0 ? (
              <p className="texto-suave">Nenhum atendimento no período.</p>
            ) : (
              <div className="tabela-container">
                <table>
                  <thead>
                    <tr>
                      <th>Barbeiro</th>
                      <th className="numero">Concluídos</th>
                      {nomesServicos.map((nome) => (
                        <th key={nome} className="numero">
                          {nome}
                        </th>
                      ))}
                      <th className="numero">Faturamento</th>
                      <th className="numero">Descontos</th>
                      <th className="numero">Ticket médio</th>
                      <th className="numero">Faltas</th>
                      <th className="numero">Cancelados</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dados.barbeiros.map((barbeiro) => (
                      <tr key={barbeiro.funcionarioId}>
                        <td>
                          <strong>{barbeiro.nome}</strong>
                        </td>
                        <td className="numero">{barbeiro.atendimentosConcluidos}</td>
                        {nomesServicos.map((nome) => (
                          <td key={nome} className="numero">
                            {barbeiro.servicos.find((servico) => servico.nome === nome)?.quantidade ?? 0}
                          </td>
                        ))}
                        <td className="numero">{formatarMoeda(barbeiro.faturamento)}</td>
                        <td className="numero">{formatarMoeda(barbeiro.descontos)}</td>
                        <td className="numero">{formatarMoeda(barbeiro.ticketMedio)}</td>
                        <td className="numero">{barbeiro.naoComparecimentos}</td>
                        <td className="numero">{barbeiro.cancelamentos}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}
    </div>
  )
}
