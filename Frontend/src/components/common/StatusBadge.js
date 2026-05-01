import React from 'react'
import { CBadge } from '@coreui/react'

const colorMap = {
  ACTIVE: 'success',
  INACTIVE: 'secondary',
  AVAILABLE: 'success',
  BUSY: 'warning',
  TOCLEAN: 'danger',
  CREATED: 'secondary',
  CONFIRMED: 'info',
  CHECKED_IN: 'primary',
  CHECKED_OUT: 'success',
  CANCELLED: 'danger',
  COMPLETED: 'success',
  PENDING: 'warning',
  FAILED: 'danger',
  REFUNDED: 'dark',
  NEW: 'secondary',
  PARTIALLY_PAID: 'warning',
  PAID: 'success',
  CARD: 'info',
  CASH: 'success',
  BANK_TRANSFER: 'primary',
  ONLINE: 'dark',
  POS: 'info',
}

const StatusBadge = ({ value }) => {
  if (!value) return <span className="text-body-secondary">—</span>
  return <CBadge color={colorMap[value] || 'secondary'}>{String(value).replaceAll('_', ' ')}</CBadge>
}

export default StatusBadge