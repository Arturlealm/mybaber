import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import { ProvedorAutenticacao } from './autenticacao/ContextoAutenticacao'
import { RotasAplicacao } from './RotasAplicacao'
import './estilos/global.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <ProvedorAutenticacao>
        <RotasAplicacao />
      </ProvedorAutenticacao>
    </BrowserRouter>
  </StrictMode>,
)
