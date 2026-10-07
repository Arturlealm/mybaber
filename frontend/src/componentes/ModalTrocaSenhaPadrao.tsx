import { useState, type FormEvent } from 'react'

import { descreverErro, requisitarApi } from '../api/clienteHttp'
import { useAutenticacao } from '../autenticacao/ContextoAutenticacao'
import { AvisoErro, AvisoSucesso } from './AvisosOperacao'

type Etapa = 'pergunta' | 'formulario' | 'concluido'

export function ModalTrocaSenhaPadrao() {
  const { sessao, dispensarAvisoSenhaPadrao } = useAutenticacao()
  const [etapa, setEtapa] = useState<Etapa>('pergunta')
  const [novaSenha, setNovaSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (sessao?.perfil !== 'CLIENTE' || !sessao.usaSenhaPadrao) {
    return null
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    if (novaSenha !== confirmacao) {
      setErro('As senhas não são iguais.')
      return
    }
    setEnviando(true)
    try {
      await requisitarApi('/api/clientes/me/senha-padrao', { metodo: 'PUT', corpo: { novaSenha } })
      setEtapa('concluido')
    } catch (falha) {
      setErro(descreverErro(falha))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="fundo-modal" role="dialog" aria-modal="true" aria-labelledby="titulo-senha-padrao">
      <div className="cartao modal pilha" style={{ maxWidth: 400 }}>
        {etapa === 'pergunta' && (
          <>
            <h2 id="titulo-senha-padrao">Troque sua senha</h2>
            <p className="texto-suave" style={{ margin: 0 }}>
              Você está usando a senha padrão da barbearia. Deseja criar uma senha só sua agora?
            </p>
            <div className="linha">
              <button type="button" className="botao" style={{ flex: 1 }} onClick={() => setEtapa('formulario')}>
                Sim
              </button>
              <button
                type="button"
                className="botao botao-secundario"
                style={{ flex: 1 }}
                onClick={dispensarAvisoSenhaPadrao}
              >
                Não
              </button>
            </div>
          </>
        )}

        {etapa === 'formulario' && (
          <form className="pilha" onSubmit={salvar}>
            <h2 id="titulo-senha-padrao">Criar nova senha</h2>
            <label className="campo">
              Nova senha
              <input
                type="password"
                value={novaSenha}
                onChange={(e) => setNovaSenha(e.target.value)}
                minLength={8}
                required
                autoFocus
                autoComplete="new-password"
              />
              <span className="campo-ajuda">Mínimo de 8 caracteres.</span>
            </label>
            <label className="campo">
              Confirme a nova senha
              <input
                type="password"
                value={confirmacao}
                onChange={(e) => setConfirmacao(e.target.value)}
                minLength={8}
                required
                autoComplete="new-password"
              />
            </label>
            <AvisoErro mensagem={erro} />
            <div className="linha">
              <button type="submit" className="botao" style={{ flex: 1 }} disabled={enviando}>
                {enviando ? 'Salvando...' : 'Salvar senha'}
              </button>
              <button
                type="button"
                className="botao botao-secundario"
                style={{ flex: 1 }}
                onClick={dispensarAvisoSenhaPadrao}
                disabled={enviando}
              >
                Agora não
              </button>
            </div>
          </form>
        )}

        {etapa === 'concluido' && (
          <>
            <h2 id="titulo-senha-padrao">Senha alterada</h2>
            <AvisoSucesso mensagem="Pronto! Da próxima vez, entre com a sua nova senha." />
            <button type="button" className="botao" onClick={dispensarAvisoSenhaPadrao}>
              Continuar
            </button>
          </>
        )}
      </div>
    </div>
  )
}
