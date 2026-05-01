import React from 'react'
import { CFooter } from '@coreui/react'

const AppFooter = () => {
  return (
    <CFooter className="px-4">
      <div>Enterprise Hotel Management System Frontend</div>
      <div className="ms-auto">CoreUI template adapted for your Spring Boot backend</div>
    </CFooter>
  )
}

export default React.memo(AppFooter)
