import type { ErroCampo, ErroRespostaApi } from './tiposApi'

const CHAVE_SESSAO = 'mybarber.sessao'
export const EVENTO_SESSAO_EXPIRADA = 'mybarber:sessao-expirada'

export class ErroApi extends Error {
  readonly status: number
  readonly campos: ErroCampo[]

  constructor(status: number, mensagem: string, campos: ErroCampo[] = []) {
    super(mensagem)
    this.status = status
    this.campos = campos
  }
}

type OpcoesRequisicao = {
  metodo?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  corpo?: unknown
  parametros?: Record<string, string | number | undefined | null | (string | number)[]>
}

export function lerTokenSalvo(): string | null {
  try {
    const sessao = localStorage.getItem(CHAVE_SESSAO)
    return sessao ? (JSON.parse(sessao).tokenAcesso as string) : null
  } catch {
    return null
  }
}

export function salvarSessao(sessao: unknown) {
  try {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao))
  } catch {
    /* Sem armazenamento local a sessão vale apenas enquanto a página estiver aberta */
  }
}

export function lerSessaoSalva<T>(): T | null {
  try {
    const sessao = localStorage.getItem(CHAVE_SESSAO)
    return sessao ? (JSON.parse(sessao) as T) : null
  } catch {
    return null
  }
}

export function removerSessao() {
  try {
    localStorage.removeItem(CHAVE_SESSAO)
  } catch {
    /* Nada a remover quando o armazenamento local não está disponível */
  }
}

function montarUrl(caminho: string, parametros: OpcoesRequisicao['parametros']) {
  const busca = new URLSearchParams()
  Object.entries(parametros ?? {}).forEach(([chave, valor]) => {
    if (valor === undefined || valor === null || valor === '') return
    busca.set(chave, Array.isArray(valor) ? valor.join(',') : String(valor))
  })
  const consulta = busca.toString()
  return consulta ? `${caminho}?${consulta}` : caminho
}

export async function requisitarApi<T>(caminho: string, opcoes: OpcoesRequisicao = {}): Promise<T> {
  const token = lerTokenSalvo()
  const resposta = await fetch(montarUrl(caminho, opcoes.parametros), {
    method: opcoes.metodo ?? 'GET',
    headers: {
      ...(opcoes.corpo !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: opcoes.corpo !== undefined ? JSON.stringify(opcoes.corpo) : undefined,
  })

  if (resposta.status === 204) {
    return undefined as T
  }

  const conteudo = await resposta.json().catch(() => null)
  if (!resposta.ok) {
    const erro = conteudo as ErroRespostaApi | null
    if (resposta.status === 401 && token) {
      window.dispatchEvent(new Event(EVENTO_SESSAO_EXPIRADA))
    }
    throw new ErroApi(resposta.status, erro?.mensagem ?? 'Não foi possível concluir a operação', erro?.campos ?? [])
  }
  return conteudo as T
}

export function descreverErro(erro: unknown): string {
  if (erro instanceof ErroApi) {
    if (erro.campos.length > 0) {
      return erro.campos.map((campo) => campo.mensagem).join('. ')
    }
    return erro.message
  }
  return 'Não foi possível conectar ao servidor. Verifique se a API está em execução.'
}
