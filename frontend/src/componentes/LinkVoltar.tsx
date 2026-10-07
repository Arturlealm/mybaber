import { Link } from 'react-router-dom'

export function LinkVoltar({ para, rotulo = 'Voltar' }: { para: string; rotulo?: string }) {
  return (
    <Link to={para} className="link-voltar" aria-label={rotulo}>
      <span aria-hidden="true">←</span> {rotulo}
    </Link>
  )
}
