const formatoMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const formatoDataLonga = new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' })
const formatoDataCurta = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
const formatoMesAno = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' })

export function formatarMoeda(valor: number | null | undefined) {
  return formatoMoeda.format(valor ?? 0)
}

export function formatarHora(horario: string) {
  const hora = horario.includes('T') ? horario.split('T')[1] : horario
  return hora.slice(0, 5)
}

export function converterDataIso(dataIso: string) {
  const [ano, mes, dia] = dataIso.slice(0, 10).split('-').map(Number)
  return new Date(ano, mes - 1, dia)
}

export function formatarDataLonga(dataIso: string) {
  return formatoDataLonga.format(converterDataIso(dataIso))
}

export function formatarDataCurta(dataIso: string) {
  return formatoDataCurta.format(converterDataIso(dataIso))
}

export function formatarMesAno(data: Date) {
  return formatoMesAno.format(data)
}

export function formatarDataIso(data: Date) {
  const ano = data.getFullYear()
  const mes = String(data.getMonth() + 1).padStart(2, '0')
  const dia = String(data.getDate()).padStart(2, '0')
  return `${ano}-${mes}-${dia}`
}

export function hojeIso() {
  return formatarDataIso(new Date())
}

export function somarDias(dataIso: string, dias: number) {
  const data = converterDataIso(dataIso)
  data.setDate(data.getDate() + dias)
  return formatarDataIso(data)
}

export function formatarTelefone(telefone: string) {
  if (telefone.length === 11) return `(${telefone.slice(0, 2)}) ${telefone.slice(2, 7)}-${telefone.slice(7)}`
  if (telefone.length === 10) return `(${telefone.slice(0, 2)}) ${telefone.slice(2, 6)}-${telefone.slice(6)}`
  return telefone
}

export function formatarDuracao(minutos: number) {
  if (minutos === 0) return 'incluso'
  if (minutos < 60) return `${minutos} min`
  const horas = Math.floor(minutos / 60)
  const resto = minutos % 60
  return resto === 0 ? `${horas}h` : `${horas}h${String(resto).padStart(2, '0')}`
}
