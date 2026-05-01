import React from 'react'
import { CButton, CCol, CRow } from '@coreui/react'

const PageHeader = ({ title, subtitle, actionLabel, onAction, actionColor = 'primary' }) => {
  return (
    <CRow className="align-items-center mb-4">
      <CCol>
        <h2 className="mb-1">{title}</h2>
        {subtitle ? <div className="text-body-secondary">{subtitle}</div> : null}
      </CCol>
      {actionLabel ? (
        <CCol xs="auto">
          <CButton color={actionColor} onClick={onAction}>
            {actionLabel}
          </CButton>
        </CCol>
      ) : null}
    </CRow>
  )
}

export default PageHeader
