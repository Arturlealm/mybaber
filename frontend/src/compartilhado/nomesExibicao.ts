import type { DiaSemana, OrigemExpediente, StatusAgendamento } from '../api/tiposApi'

export const NOME_STATUS_AGENDAMENTO: Record<StatusAgendamento, string> = {
  AGENDADO: 'Agendado',
  CONCLUIDO: 'Concluído',
  CANCELADO: 'Cancelado',
  NAO_COMPARECEU: 'Não compareceu',
}

export const NOME_DIA_SEMANA: Record<DiaSemana, string> = {
  SEGUNDA: 'Segunda',
  TERCA: 'Terça',
  QUARTA: 'Quarta',
  QUINTA: 'Quinta',
  SEXTA: 'Sexta',
  SABADO: 'Sábado',
  DOMINGO: 'Domingo',
}

export const NOME_ORIGEM_EXPEDIENTE: Record<OrigemExpediente, string> = {
  JORNADA_FILIAL: 'Horário padrão da barbearia',
  JORNADA_FUNCIONARIO: 'Horário próprio do barbeiro',
  AJUSTE_GERAL: 'Ajuste para todos',
  AJUSTE_FUNCIONARIO: 'Ajuste individual',
}
