export type PerfilAcesso = 'CLIENTE' | 'BARBEIRO' | 'ADMINISTRADOR'

export type TokenAcesso = {
  tokenAcesso: string
  tipoToken: string
  expiraEm: string
  perfil: PerfilAcesso
  nome: string
}

export type ErroCampo = {
  campo: string
  mensagem: string
}

export type ErroRespostaApi = {
  status: number
  mensagem: string
  campos: ErroCampo[]
}

export type Pagina<T> = {
  conteudo: T[]
  pagina: number
  tamanho: number
  totalElementos: number
  totalPaginas: number
}

export type Cliente = {
  id: number
  nome: string
  email: string
  cpf: string | null
  telefone: string
  ativo: boolean
}

export type TipoFuncionario = 'BARBEIRO' | 'ADMINISTRADOR'

export type Funcionario = {
  id: number
  nome: string
  email: string
  cpf: string | null
  telefone: string
  tipo: TipoFuncionario
  realizaAtendimentos: boolean
  filialId: number
  filialNome: string
  ativo: boolean
}

export type BarbeiroResumo = {
  id: number
  nome: string
  filialId: number
}

export type ServicoOferecido = {
  id: number
  nome: string
  descricao: string | null
  duracaoMinutos: number
  preco: number
  ordemExibicao: number
}

export type CombinacaoServico = {
  id: number
  nome: string
  descricao: string | null
  duracaoTotalMinutos: number
  precoTotal: number
  servicos: ServicoOferecido[]
}

export type HorariosDisponiveis = {
  data: string
  funcionarioId: number
  duracaoTotalMinutos: number
  valorTabela: number
  horarios: string[]
}

export type StatusAgendamento = 'AGENDADO' | 'CONCLUIDO' | 'CANCELADO' | 'NAO_COMPARECEU'

export type Agendamento = {
  id: number
  cliente: { id: number; nome: string; telefone: string }
  barbeiro: { id: number; nome: string }
  filialId: number
  inicio: string
  fim: string
  status: StatusAgendamento
  itens: { servicoId: number; nome: string; duracaoMinutos: number; precoTabela: number }[]
  valorTabela: number
  valorCobrado: number | null
  desconto: number | null
  observacao: string | null
  canceladoPor: 'CLIENTE' | 'FUNCIONARIO' | null
  motivoCancelamento: string | null
}

export type DiaSemana = 'SEGUNDA' | 'TERCA' | 'QUARTA' | 'QUINTA' | 'SEXTA' | 'SABADO' | 'DOMINGO'

export type SituacaoExpediente = 'ABERTO' | 'FECHADO'

export type OrigemExpediente = 'JORNADA_FILIAL' | 'JORNADA_FUNCIONARIO' | 'AJUSTE_GERAL' | 'AJUSTE_FUNCIONARIO'

export type TipoAjusteAgenda = 'ABERTO' | 'FECHADO'

export type AjusteAgenda = {
  id: number
  data: string
  tipo: TipoAjusteAgenda
  filialId: number
  funcionarioId: number | null
  todosFuncionarios: boolean
  horaInicio: string | null
  horaFim: string | null
  motivo: string | null
}

export type IntervaloHorario = {
  inicio: string
  fim: string
}

export type CalendarioAgendaDia = {
  data: string
  diaSemana: DiaSemana
  ajusteGeral: AjusteAgenda | null
  funcionarios: {
    funcionarioId: number
    nome: string
    situacao: SituacaoExpediente
    origem: OrigemExpediente
    ajusteId: number | null
    motivo: string | null
    intervalos: IntervaloHorario[]
  }[]
}

export type DestaqueRelatorio = {
  funcionarioId: number
  nomeFuncionario: string
  servicoId: number | null
  nomeServico: string | null
  quantidade: number
  valor: number | null
}

export type RelatorioDesempenhoBarbeiros = {
  inicio: string
  fim: string
  filialId: number
  totais: { atendimentosConcluidos: number; faturamento: number; descontos: number }
  maisAtendimentos: DestaqueRelatorio | null
  maiorFaturamento: DestaqueRelatorio | null
  maisAtendimentosPorServico: DestaqueRelatorio[]
  barbeiros: {
    funcionarioId: number
    nome: string
    atendimentosConcluidos: number
    faturamento: number
    descontos: number
    ticketMedio: number
    naoComparecimentos: number
    cancelamentos: number
    agendados: number
    servicos: { servicoId: number; nome: string; quantidade: number }[]
  }[]
}
