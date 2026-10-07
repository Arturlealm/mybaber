import { useEffect, useState } from 'react'

import { descreverErro, requisitarApi } from '../../api/clienteHttp'
import type {
  BarbeiroResumo,
  Cliente,
  CombinacaoServico,
  Funcionario,
  HorariosDisponiveis,
  Pagina,
} from '../../api/tiposApi'
import { AvisoErro } from '../../componentes/AvisosOperacao'
import {
  formatarDuracao,
  formatarHora,
  formatarMoeda,
  formatarTelefone,
  hojeIso,
  somarDias,
} from '../../compartilhado/formatadores'
import { useConsultaApi } from '../../compartilhado/useConsultaApi'
import { FormularioClienteBalcao } from './FormularioClienteBalcao'

type Propriedades = {
  dataInicial: string
  ehAdministrador: boolean
  aoFechar: () => void
  aoAgendar: (mensagem: string) => void
}

const ATRASO_BUSCA_MS = 300
const DIAS_MAXIMOS_RETROATIVOS_ADMINISTRADOR = 90

function horarioJaPassou(data: string, horario: string) {
  return new Date(`${data}T${horario}`).getTime() <= Date.now()
}

export function ModalAgendamentoBalcao({ dataInicial, ehAdministrador, aoFechar, aoAgendar }: Propriedades) {
  const [busca, setBusca] = useState('')
  const [clientesEncontrados, setClientesEncontrados] = useState<Cliente[]>([])
  const [buscaRealizada, setBuscaRealizada] = useState(false)
  const [cadastrandoCliente, setCadastrandoCliente] = useState(false)
  const [senhaInicialCliente, setSenhaInicialCliente] = useState<string | null>(null)
  const [cliente, setCliente] = useState<Cliente | null>(null)
  const [combinacao, setCombinacao] = useState<CombinacaoServico | null>(null)
  const [barbeiroId, setBarbeiroId] = useState<number | null>(null)
  const [data, setData] = useState(dataInicial)
  const [horario, setHorario] = useState<string | null>(null)
  const [atendimentoRealizado, setAtendimentoRealizado] = useState(true)
  const [houveDesconto, setHouveDesconto] = useState(false)
  const [valorCobrado, setValorCobrado] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const dataMinima = ehAdministrador ? somarDias(hojeIso(), -DIAS_MAXIMOS_RETROATIVOS_ADMINISTRADOR) : hojeIso()

  const combinacoes = useConsultaApi(() => requisitarApi<CombinacaoServico[]>('/api/servicos/combinacoes'), [])
  const barbeiros = useConsultaApi(
    ehAdministrador ? () => requisitarApi<BarbeiroResumo[]>('/api/funcionarios/barbeiros') : null,
    [ehAdministrador],
  )
  const funcionarioAutenticado = useConsultaApi(
    ehAdministrador ? null : () => requisitarApi<Funcionario>('/api/funcionarios/me'),
    [ehAdministrador],
  )
  const idBarbeiro = ehAdministrador ? barbeiroId : (funcionarioAutenticado.dados?.id ?? null)

  const horarios = useConsultaApi(
    combinacao && idBarbeiro
      ? () =>
          requisitarApi<HorariosDisponiveis>('/api/agendamentos/horarios-disponiveis', {
            parametros: { funcionarioId: idBarbeiro, data, servicoIds: combinacao.servicos.map((servico) => servico.id) },
          })
      : null,
    [combinacao?.id, idBarbeiro, data],
  )

  const horarioEscolhidoJaPassou = horario !== null && horarioJaPassou(data, horario)
  const registrarComoRealizado = horarioEscolhidoJaPassou && atendimentoRealizado
  const valorInformado = Number(valorCobrado.replace(',', '.'))
  const valorInformadoValido = valorCobrado.trim() !== '' && Number.isFinite(valorInformado) && valorInformado >= 0

  useEffect(() => {
    if (cliente || busca.trim().length < 2) {
      setClientesEncontrados([])
      setBuscaRealizada(false)
      return
    }
    const temporizador = window.setTimeout(() => {
      requisitarApi<Pagina<Cliente>>('/api/clientes', { parametros: { busca: busca.trim(), size: 8 } })
        .then((pagina) => {
          setClientesEncontrados(pagina.conteudo)
          setBuscaRealizada(true)
        })
        .catch((falha) => setErro(descreverErro(falha)))
    }, ATRASO_BUSCA_MS)
    return () => window.clearTimeout(temporizador)
  }, [busca, cliente])

  function escolherHorario(opcao: string) {
    setHorario(opcao)
    setAtendimentoRealizado(true)
    setHouveDesconto(false)
    setValorCobrado('')
  }

  async function confirmar() {
    if (!cliente || !combinacao || !idBarbeiro || !horario) return
    setErro(null)
    setEnviando(true)
    try {
      await requisitarApi('/api/agendamentos', {
        metodo: 'POST',
        corpo: {
          clienteId: cliente.id,
          funcionarioId: idBarbeiro,
          inicio: `${data}T${horario}`,
          servicoIds: combinacao.servicos.map((servico) => servico.id),
          atendimentoRealizado: registrarComoRealizado,
          valorCobrado: registrarComoRealizado && houveDesconto ? valorInformado : null,
        },
      })
      aoAgendar(registrarComoRealizado ? 'Atendimento registrado.' : 'Agendamento criado.')
    } catch (falha) {
      setErro(descreverErro(falha))
      horarios.recarregar()
    } finally {
      setEnviando(false)
    }
  }

  const podeConfirmar =
    !!cliente && !!combinacao && !!idBarbeiro && !!horario && (!registrarComoRealizado || !houveDesconto || valorInformadoValido)

  return (
    <div className="fundo-modal" role="dialog" aria-modal="true" aria-labelledby="titulo-agendamento-balcao">
      <div className="cartao modal pilha" style={{ maxWidth: 560 }}>
        <h2 id="titulo-agendamento-balcao">Novo agendamento</h2>

        <div className="pilha">
          <h3>Cliente</h3>
          {cliente ? (
            <>
              <div className="item-lista">
                <span>
                  <strong>{cliente.nome}</strong>{' '}
                  <span className="texto-suave">· {formatarTelefone(cliente.telefone)}</span>
                </span>
                <button
                  type="button"
                  className="botao botao-secundario botao-pequeno"
                  onClick={() => {
                    setCliente(null)
                    setSenhaInicialCliente(null)
                  }}
                >
                  Trocar
                </button>
              </div>
              {senhaInicialCliente && (
                <div className="aviso aviso-sucesso">
                  Cliente cadastrado. Informe o acesso ao site: email <strong>{cliente.email}</strong> e senha{' '}
                  <strong>{senhaInicialCliente}</strong>. Recomende trocar a senha em "Esqueci minha senha".
                </div>
              )}
            </>
          ) : cadastrandoCliente ? (
            <FormularioClienteBalcao
              buscaInicial={busca.trim()}
              aoCadastrar={(novoCliente, senhaInicial) => {
                setCliente(novoCliente)
                setSenhaInicialCliente(senhaInicial)
                setCadastrandoCliente(false)
                setBusca('')
              }}
              aoCancelar={() => setCadastrandoCliente(false)}
            />
          ) : (
            <>
              <label className="campo">
                Buscar por nome ou telefone
                <input value={busca} onChange={(e) => setBusca(e.target.value)} autoFocus />
              </label>
              <ul className="lista">
                {clientesEncontrados.map((encontrado) => (
                  <li key={encontrado.id}>
                    <button
                      type="button"
                      className="opcao-selecao"
                      style={{ width: '100%' }}
                      onClick={() => {
                        setCliente(encontrado)
                        setBusca('')
                      }}
                    >
                      <strong>{encontrado.nome}</strong>
                      <span className="campo-ajuda">
                        {formatarTelefone(encontrado.telefone)}
                        {encontrado.email && ` · ${encontrado.email}`}
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
              {buscaRealizada && clientesEncontrados.length === 0 && (
                <p className="campo-ajuda">
                  Nenhum cliente encontrado.
                  {!ehAdministrador && ' Peça ao administrador para cadastrar o cliente.'}
                </p>
              )}
              {ehAdministrador && (
                <button
                  type="button"
                  className="botao botao-secundario"
                  onClick={() => setCadastrandoCliente(true)}
                >
                  + Cadastrar cliente novo
                </button>
              )}
            </>
          )}
        </div>

        <div className="pilha">
          <h3>Serviço</h3>
          <div className="opcoes-selecao">
            {combinacoes.dados?.map((opcao) => (
              <button
                key={opcao.id}
                type="button"
                className="opcao-selecao"
                aria-pressed={combinacao?.id === opcao.id}
                onClick={() => {
                  setCombinacao(opcao)
                  setHorario(null)
                }}
              >
                <strong>{opcao.nome}</strong>
                <span className="campo-ajuda">
                  {formatarDuracao(opcao.duracaoTotalMinutos)} · {formatarMoeda(opcao.precoTotal)}
                </span>
              </button>
            ))}
          </div>
        </div>

        <div className="linha">
          {ehAdministrador && (
            <label className="campo" style={{ flex: 1 }}>
              Barbeiro
              <select
                value={barbeiroId ?? ''}
                onChange={(e) => {
                  setBarbeiroId(e.target.value ? Number(e.target.value) : null)
                  setHorario(null)
                }}
              >
                <option value="">Selecione</option>
                {barbeiros.dados?.map((barbeiro) => (
                  <option key={barbeiro.id} value={barbeiro.id}>
                    {barbeiro.nome}
                  </option>
                ))}
              </select>
            </label>
          )}
          <label className="campo" style={{ flex: 1 }}>
            Dia
            <input
              type="date"
              min={dataMinima}
              value={data}
              onChange={(e) => {
                if (!e.target.value) return
                setData(e.target.value)
                setHorario(null)
              }}
            />
          </label>
        </div>

        {combinacao && idBarbeiro && (
          <div className="pilha">
            <h3>Horário</h3>
            <AvisoErro mensagem={horarios.erro} />
            {horarios.dados?.horarios.length === 0 && <p className="texto-suave">Sem horários livres neste dia.</p>}
            {horarios.dados?.horarios.some((opcao) => horarioJaPassou(data, opcao)) && (
              <p className="campo-ajuda">Horários tracejados já passaram e podem ser usados para registrar atendimentos feitos.</p>
            )}
            <div className="horarios">
              {horarios.dados?.horarios.map((opcao) => (
                <button
                  key={opcao}
                  type="button"
                  className={`horario ${horarioJaPassou(data, opcao) ? 'horario-passado' : ''}`}
                  aria-pressed={horario === opcao}
                  onClick={() => escolherHorario(opcao)}
                >
                  {formatarHora(opcao)}
                </button>
              ))}
            </div>
          </div>
        )}

        {horarioEscolhidoJaPassou && horarios.dados && (
          <div className="pilha cartao" style={{ background: 'var(--cor-superficie-suave)' }}>
            <label className="opcao-marcavel">
              <input
                type="checkbox"
                checked={atendimentoRealizado}
                onChange={(e) => setAtendimentoRealizado(e.target.checked)}
              />
              <strong>Atendimento já realizado</strong>
            </label>
            {atendimentoRealizado && (
              <>
                <div className="resumo-valor">
                  <span>Confirme o valor do serviço</span>
                  <strong>{formatarMoeda(horarios.dados.valorTabela)}</strong>
                </div>
                <label className="opcao-marcavel">
                  <input type="checkbox" checked={houveDesconto} onChange={(e) => setHouveDesconto(e.target.checked)} />
                  Houve desconto? Informe o valor cobrado
                </label>
                {houveDesconto && (
                  <label className="campo">
                    Valor cobrado (R$)
                    <input
                      inputMode="decimal"
                      value={valorCobrado}
                      onChange={(e) => setValorCobrado(e.target.value)}
                      placeholder="Ex.: 60,00"
                    />
                  </label>
                )}
              </>
            )}
          </div>
        )}

        <AvisoErro mensagem={erro} />
        <div className="linha">
          <button type="button" className="botao" onClick={confirmar} disabled={enviando || !podeConfirmar}>
            {enviando ? 'Salvando...' : registrarComoRealizado ? 'Registrar atendimento' : 'Agendar'}
          </button>
          <button type="button" className="botao botao-secundario" onClick={aoFechar} disabled={enviando}>
            Cancelar
          </button>
        </div>
      </div>
    </div>
  )
}
