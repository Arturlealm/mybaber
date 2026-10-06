export function AvisoErro({ mensagem }: { mensagem: string | null }) {
  if (!mensagem) return null
  return (
    <div className="aviso aviso-erro" role="alert">
      {mensagem}
    </div>
  )
}

export function AvisoSucesso({ mensagem }: { mensagem: string | null }) {
  if (!mensagem) return null
  return (
    <div className="aviso aviso-sucesso" role="status">
      {mensagem}
    </div>
  )
}
