import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type {
  Agendamento,
  BarbeiroResumo,
  CombinacaoServico,
  HorariosDisponiveis,
  ServicoOferecido,
} from '../../api/tiposApi'
import { AvisoErro } from '../../componentes/AvisosOperacao'
import {
  converterDataIso,
  formatarDataLonga,
  formatarDuracao,
  formatarHora,
  formatarMoeda,
  hojeIso,
  somarDias,
} from '../../compartilhado/formatadores'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'

const QUANTIDADE_DIAS_EXIBIDOS = 14
const NOMES_DIAS_CURTOS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']

export function PaginaNovoAgendamento() {
  const [combinacao, setCombinacao] = useState<CombinacaoServico | null>(null)
  const [barbeiro, setBarbeiro] = useState<BarbeiroResumo | null>(null)
  const [data, setData] = useState(hojeIso())
  const [horario, setHorario] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [agendamentoCriado, setAgendamentoCriado] = useState<Agendamento | null>(null)

  const combinacoes = useConsultaApi(() => requisitarApi<CombinacaoServico[]>('/api/servicos/combinacoes'), [])
  const servicos = useConsultaApi(() => requisitarApi<ServicoOferecido[]>('/api/servicos'), [])
  const barbeiros = useConsultaApi(() => requisitarApi<BarbeiroResumo[]>('/api/funcionarios/barbeiros'), [])
  const horarios = useConsultaApi(
    combinacao && barbeiro
      ? () =>
          requisitarApi<HorariosDisponiveis>('/api/agendamentos/horarios-disponiveis', {
            parametros: {
              funcionarioId: barbeiro.id,
              data,
              servicoIds: combinacao.servicos.map((servico) => servico.id),
            },
          })
      : null,
    [combinacao?.id, barbeiro?.id, data, agendamentoCriado?.id],
  )

  const dias = useMemo(
    () => Array.from({ length: QUANTIDADE_DIAS_EXIBIDOS }, (_, indice) => somarDias(hojeIso(), indice)),
    [],
  )

  function escolherCombinacao(opcao: CombinacaoServico) {
    setCombinacao(opcao)
    setHorario(null)
  }

  function escolherBarbeiro(opcao: BarbeiroResumo) {
    setBarbeiro(opcao)
    setHorario(null)
  }

  function escolherData(opcao: string) {
    setData(opcao)
    setHorario(null)
  }

  async function confirmar() {
    if (!combinacao || !barbeiro || !horario) return
    setErro(null)
    setEnviando(true)
    try {
      const agendamento = await requisitarApi<Agendamento>('/api/agendamentos', {
        metodo: 'POST',
        corpo: {
          funcionarioId: barbeiro.id,
          inicio: `${data}T${horario}`,
          servicoIds: combinacao.servicos.map((servico) => servico.id),
        },
      })
      setAgendamentoCriado(agendamento)
      setHorario(null)
    } catch (falha) {
      setErro(descreverErro(falha))
      horarios.recarregar()
    } finally {
      setEnviando(false)
    }
  }

  if (agendamentoCriado) {
    return (
      <div className="cartao pilha" style={{ maxWidth: 520, margin: '0 auto' }}>
        <h1>Agendamento confirmado!</h1>
        <p>
          <strong>{agendamentoCriado.itens.map((item) => item.nome).join(', ')}</strong> com{' '}
          <strong>{agendamentoCriado.barbeiro.nome}</strong>
        </p>
        <p>
          {formatarDataLonga(agendamentoCriado.inicio)}, das {formatarHora(agendamentoCriado.inicio)} às{' '}
          {formatarHora(agendamentoCriado.fim)}
        </p>
        <div className="resumo-valor">
          <span>Valor</span>
          <strong>{formatarMoeda(agendamentoCriado.valorTabela)}</strong>
        </div>
        <p className="texto-suave">O pagamento é feito diretamente na barbearia.</p>
        <div className="linha">
          <Link to="/meus-agendamentos" className="botao">
            Ver meus agendamentos
          </Link>
          <button type="button" className="botao botao-secundario" onClick={() => setAgendamentoCriado(null)}>
            Fazer outro agendamento
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="pilha">
      <div className="titulo-pagina">
        <div>
          <h1>Agendar horário</h1>
          <p>Escolha o serviço, o barbeiro e o melhor horário para você.</p>
        </div>
      </div>

      <section className="cartao pilha">
        <h2>1. Qual será o serviço?</h2>
        <AvisoErro mensagem={combinacoes.erro} />
        <div className="opcoes-selecao">
          {combinacoes.dados?.map((opcao) => (
            <button
              key={opcao.id}
              type="button"
              className="opcao-selecao"
              aria-pressed={combinacao?.id === opcao.id}
              onClick={() => escolherCombinacao(opcao)}
            >
              <strong>{opcao.nome}</strong>
              <span className="texto-suave">
                {formatarDuracao(opcao.duracaoTotalMinutos)} · {formatarMoeda(opcao.precoTotal)}
              </span>
              {opcao.servicos.length > 1 && (
                <span className="campo-ajuda">{opcao.servicos.map((servico) => servico.nome).join(' + ')}</span>
              )}
            </button>
          ))}
        </div>
        {servicos.dados && (
          <p className="campo-ajuda">
            Serviços da casa:{' '}
            {servicos.dados
              .map((servico) =>
                servico.duracaoMinutos === 0
                  ? `${servico.nome} (incluso no cabelo e barba)`
                  : `${servico.nome} ${formatarMoeda(servico.preco)}`,
              )
              .join(' · ')}
          </p>
        )}
      </section>

      <section className="cartao pilha">
        <h2>2. Com qual barbeiro?</h2>
        <AvisoErro mensagem={barbeiros.erro} />
        <div className="opcoes-selecao">
          {barbeiros.dados?.map((opcao) => (
            <button
              key={opcao.id}
              type="button"
              className="opcao-selecao"
              aria-pressed={barbeiro?.id === opcao.id}
              onClick={() => escolherBarbeiro(opcao)}
            >
              <strong>{opcao.nome}</strong>
            </button>
          ))}
        </div>
        {barbeiros.dados?.length === 0 && <p className="texto-suave">Nenhum barbeiro disponível no momento.</p>}
      </section>

      <section className="cartao pilha">
        <h2>3. Qual dia?</h2>
        <div className="horarios" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(72px, 1fr))' }}>
          {dias.map((dia) => {
            const dataDia = converterDataIso(dia)
            return (
              <button
                key={dia}
                type="button"
                className="horario"
                aria-pressed={data === dia}
                onClick={() => escolherData(dia)}
              >
                <small style={{ display: 'block', fontWeight: 400 }}>{NOMES_DIAS_CURTOS[dataDia.getDay()]}</small>
                {String(dataDia.getDate()).padStart(2, '0')}/{String(dataDia.getMonth() + 1).padStart(2, '0')}
              </button>
            )
          })}
        </div>
      </section>

      <section className="cartao pilha">
        <h2>4. Qual horário?</h2>
        {!combinacao || !barbeiro ? (
          <p className="texto-suave">Escolha o serviço e o barbeiro para ver os horários livres.</p>
        ) : (
          <>
            <p className="texto-suave">{formatarDataLonga(data)}</p>
            <AvisoErro mensagem={horarios.erro} />
            {horarios.carregando && <p className="texto-suave">Carregando horários...</p>}
            {horarios.dados && horarios.dados.horarios.length === 0 && (
              <p className="texto-suave">Não há horários livres neste dia. Tente outro dia ou outro barbeiro.</p>
            )}
            <div className="horarios">
              {horarios.dados?.horarios.map((opcao) => (
                <button
                  key={opcao}
                  type="button"
                  className="horario"
                  aria-pressed={horario === opcao}
                  onClick={() => setHorario(opcao)}
                >
                  {formatarHora(opcao)}
                </button>
              ))}
            </div>
          </>
        )}
      </section>

      {combinacao && barbeiro && horario && (
        <section className="cartao pilha">
          <h2>Confirmar</h2>
          <p>
            <strong>{combinacao.nome}</strong> com <strong>{barbeiro.nome}</strong>, {formatarDataLonga(data)} às{' '}
            <strong>{formatarHora(horario)}</strong> ({formatarDuracao(combinacao.duracaoTotalMinutos)})
          </p>
          <div className="resumo-valor">
            <span>Valor</span>
            <strong>{formatarMoeda(combinacao.precoTotal)}</strong>
          </div>
          <AvisoErro mensagem={erro} />
          <button type="button" className="botao" onClick={confirmar} disabled={enviando}>
            {enviando ? 'Agendando...' : 'Confirmar agendamento'}
          </button>
        </section>
      )}
    </div>
  )
}
