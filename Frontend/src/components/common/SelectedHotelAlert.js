import React from 'react'
import { CAlert } from '@coreui/react'

const SelectedHotelAlert = () => (
  <CAlert color="warning" className="mb-4">
    This page requires a selected hotel. Log in with a user that has hotel access or choose a hotel in the header.
  </CAlert>
)

export default SelectedHotelAlert
