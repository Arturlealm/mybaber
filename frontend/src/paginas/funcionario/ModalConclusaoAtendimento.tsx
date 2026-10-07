import { useState } from 'react'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type { Agendamento } from '../../api/tiposApi'
import { AvisoErro } from '../../componentes/AvisosOperacao'
import { formatarHora, formatarMoeda } from '../../compartilhado/formatadores'

type Propriedades = {
  agendamento: Agendamento
  aoFechar: () => void
  aoConcluir: () => void
}

export function ModalConclusaoAtendimento({ agendamento, aoFechar, aoConcluir }: Propriedades) {
  const [houveDesconto, setHouveDesconto] = useState(false)
  const [valorCobrado, setValorCobrado] = useState('')
  const [observacao, setObservacao] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const valorInformado = Number(valorCobrado.replace(',', '.'))
  const valorInformadoValido = valorCobrado.trim() !== '' && Number.isFinite(valorInformado) && valorInformado >= 0

  async function concluir(valor: number | null) {
    setErro(null)
    setEnviando(true)
    try {
      await requisitarApi(`/api/agendamentos/${agendamento.id}/conclusao`, {
        metodo: 'PATCH',
        corpo: { valorCobrado: valor, observacao: observacao || null },
      })
      aoConcluir()
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="fundo-modal" role="dialog" aria-modal="true" aria-labelledby="titulo-conclusao">
      <div className="cartao modal pilha">
        <div>
          <h2 id="titulo-conclusao">Concluir atendimento</h2>
          <p className="texto-suave">
            {agendamento.cliente.nome} · {formatarHora(agendamento.inicio)} ·{' '}
            {agendamento.itens.map((item) => item.nome).join(', ')}
          </p>
        </div>

        <div className="pilha">
          <h3>Confirme o valor do serviço</h3>
          <div className="resumo-valor">
            <span>Valor de tabela</span>
            <strong>{formatarMoeda(agendamento.valorTabela)}</strong>
          </div>
          {!houveDesconto && (
            <button type="button" className="botao" onClick={() => concluir(null)} disabled={enviando}>
              Confirmar {formatarMoeda(agendamento.valorTabela)}
            </button>
          )}
        </div>

        <label className="opcao-marcavel">
          <input type="checkbox" checked={houveDesconto} onChange={(e) => setHouveDesconto(e.target.checked)} />
          Houve desconto? Informe o valor cobrado
        </label>

        {houveDesconto && (
          <div className="pilha">
            <label className="campo">
              Valor cobrado (R$)
              <input
                inputMode="decimal"
                value={valorCobrado}
                onChange={(e) => setValorCobrado(e.target.value)}
                placeholder="Ex.: 60,00"
                autoFocus
              />
            </label>
            {valorInformadoValido && valorInformado < agendamento.valorTabela && (
              <p className="campo-ajuda">
                Desconto de {formatarMoeda(agendamento.valorTabela - valorInformado)}
              </p>
            )}
            <label className="campo">
              <span>
                Observação <span className="campo-ajuda">(opcional)</span>
              </span>
              <input value={observacao} onChange={(e) => setObservacao(e.target.value)} maxLength={255} />
            </label>
            <button
              type="button"
              className="botao"
              onClick={() => concluir(valorInformado)}
              disabled={enviando || !valorInformadoValido}
            >
              Concluir com {valorInformadoValido ? formatarMoeda(valorInformado) : 'o valor informado'}
            </button>
          </div>
        )}

        <AvisoErro mensagem={erro} />
        <button type="button" className="botao botao-secundario" onClick={aoFechar} disabled={enviando}>
          Voltar
        </button>
      </div>
    </div>
  )
}
