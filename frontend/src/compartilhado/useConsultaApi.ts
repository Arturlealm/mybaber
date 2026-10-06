import { useCallback, useEffect, useState } from 'react'

import { descreverErro } from '../api/clienteHttp'

export function useConsultaApi<T>(consultar: (() => Promise<T>) | null, dependencias: unknown[]) {
  const [dados, setDados] = useState<T | null>(null)
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [versao, setVersao] = useState(0)

  useEffect(() => {
    if (!consultar) {
      setDados(null)
      return
    }
    let ativo = true
    setCarregando(true)
    setErro(null)
    consultar()
      .then((resultado) => ativo && setDados(resultado))
      .catch((falha) => ativo && setErro(descreverErro(falha)))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [...dependencias, versao])

  const recarregar = useCallback(() => setVersao((atual) => atual + 1), [])

  return { dados, carregando, erro, recarregar }
}
