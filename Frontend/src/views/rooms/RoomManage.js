import React, { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  CAlert,
  CBadge,
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CForm,
  CFormInput,
  CFormLabel,
  CFormSelect,
  CNav,
  CNavItem,
  CNavLink,
  CRow,
  CSpinner,
  CTabContent,
  CTabPane,
} from '@coreui/react'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../context/AuthContext'
import { apiFetch } from '../../services/api'

const occupancyOptions = [
  'AVAILABLE',
  'OCCUPIED',
  'RESERVED',
  'BLOCKED',
  'CHECKOUT_TODAY',
  'ARRIVAL_TODAY',
]

const housekeepingOptions = [
  'CLEAN',
  'DIRTY',
  'TO_CLEAN',
  'INSPECTED',
  'CLEANING',
]

const technicalOptions = [
  'ACTIVE',
  'OUT_OF_SERVICE',
  'OUT_OF_ORDER',
  'INACTIVE',
]

const badgeColor = (value) => {
  switch (value) {
    case 'AVAILABLE':
    case 'CLEAN':
    case 'ACTIVE':
    case true:
      return 'success'
    case 'OCCUPIED':
    case 'RESERVED':
    case 'INSPECTED':
      return 'info'
    case 'DIRTY':
    case 'TO_CLEAN':
    case 'CLEANING':
    case 'CHECKOUT_TODAY':
    case 'ARRIVAL_TODAY':
      return 'warning'
    case 'BLOCKED':
    case 'OUT_OF_SERVICE':
    case 'OUT_OF_ORDER':
    case 'INACTIVE':
    case false:
      return 'danger'
    default:
      return 'secondary'
  }
}

const StatusPill = ({ value }) => (
  <CBadge color={badgeColor(value)} shape="rounded-pill">
    {String(value ?? '—')}
  </CBadge>
)

const RoomManage = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const { token } = useAuth()

  const [activeTab, setActiveTab] = useState('general')
  const [room, setRoom] = useState(null)
  const [roomTypes, setRoomTypes] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const [generalForm, setGeneralForm] = useState({
    roomTypeId: '',
    roomNumber: '',
    floor: '',
    capacity: '',
    basePrice: '',
    description: '',
    notes: '',
    active: true,
  })

  const [statusForm, setStatusForm] = useState({
    occupancyStatus: '',
    housekeepingStatus: '',
    technicalStatus: '',
  })

  const roomTypeOptions = useMemo(
    () => roomTypes.map((type) => ({ value: String(type.id), label: type.name })),
    [roomTypes],
  )

  const loadRoomTypes = async () => {
    const data = await apiFetch('/api/room-types', {}, token)
    setRoomTypes(Array.isArray(data) ? data : [])
  }

  const loadRoom = async () => {
    const data = await apiFetch(`/api/rooms/${id}`, {}, token)
    setRoom(data)

    setGeneralForm({
      roomTypeId: data.roomTypeId ? String(data.roomTypeId) : '',
      roomNumber: data.roomNumber || '',
      floor: data.floor ?? '',
      capacity: data.capacity ?? '',
      basePrice: data.basePrice ?? '',
      description: data.description || '',
      notes: data.notes || '',
      active: Boolean(data.active),
    })

    setStatusForm({
      occupancyStatus: data.occupancyStatus || '',
      housekeepingStatus: data.housekeepingStatus || '',
      technicalStatus: data.technicalStatus || '',
    })
  }

  const refreshAll = async () => {
    try {
      setLoading(true)
      setError('')
      await Promise.all([loadRoomTypes(), loadRoom()])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    refreshAll()
  }, [id])

  const saveGeneral = async (event) => {
    event.preventDefault()

    try {
      setSaving(true)
      setError('')

      await apiFetch(
        `/api/rooms/${id}`,
        {
          method: 'PUT',
          body: JSON.stringify({
            roomTypeId: generalForm.roomTypeId ? Number(generalForm.roomTypeId) : null,
            roomNumber: generalForm.roomNumber.trim(),
            floor: generalForm.floor === '' ? null : Number(generalForm.floor),
            capacity: generalForm.capacity === '' ? null : Number(generalForm.capacity),
            basePrice: generalForm.basePrice === '' ? null : Number(generalForm.basePrice),
            description: generalForm.description || null,
            notes: generalForm.notes || null,
            active: Boolean(generalForm.active),
          }),
        },
        token,
      )

      await refreshAll()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const saveOccupancy = async () => {
    try {
      setSaving(true)
      setError('')

      await apiFetch(
        `/api/rooms/${id}/occupancy`,
        {
          method: 'PATCH',
          body: JSON.stringify({
            occupancyStatus: statusForm.occupancyStatus,
          }),
        },
        token,
      )

      await refreshAll()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const saveHousekeeping = async () => {
    try {
      setSaving(true)
      setError('')

      await apiFetch(
        `/api/rooms/${id}/housekeeping`,
        {
          method: 'PATCH',
          body: JSON.stringify({
            housekeepingStatus: statusForm.housekeepingStatus,
          }),
        },
        token,
      )

      await refreshAll()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const saveTechnical = async () => {
    try {
      setSaving(true)
      setError('')

      await apiFetch(
        `/api/rooms/${id}/technical`,
        {
          method: 'PATCH',
          body: JSON.stringify({
            technicalStatus: statusForm.technicalStatus,
          }),
        },
        token,
      )

      await refreshAll()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const markClean = async () => {
    try {
      setSaving(true)
      setError('')
      await apiFetch(`/api/rooms/${id}/mark-clean`, { method: 'PATCH' }, token)
      await refreshAll()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const markDirty = async () => {
    try {
      setSaving(true)
      setError('')
      await apiFetch(`/api/rooms/${id}/mark-dirty`, { method: 'PATCH' }, token)
      await refreshAll()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const deactivateRoom = async () => {
    if (!window.confirm('Deactivate this room?')) return

    try {
      setSaving(true)
      setError('')
      await apiFetch(`/api/rooms/${id}`, { method: 'DELETE' }, token)
      navigate('/rooms')
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <CSpinner color="primary" />
  }

  if (!room) {
    return <CAlert color="danger">Room not found.</CAlert>
  }

  return (
    <>
      <PageHeader
        title={`Manage Room ${room.roomNumber}`}
        subtitle={`${room.hotelName || 'Hotel'} — ${room.roomTypeName || 'Room'}`}
        actionLabel="Back to rooms"
        onAction={() => navigate('/rooms')}
      />

      {error ? <CAlert color="danger">{error}</CAlert> : null}

      <CCard className="mb-4">
        <CCardBody>
          <CRow className="g-3">
            <CCol md={3}>
              <div className="text-medium-emphasis small">Occupancy</div>
              <StatusPill value={room.occupancyStatus} />
            </CCol>
            <CCol md={3}>
              <div className="text-medium-emphasis small">Housekeeping</div>
              <StatusPill value={room.housekeepingStatus} />
            </CCol>
            <CCol md={3}>
              <div className="text-medium-emphasis small">Technical</div>
              <StatusPill value={room.technicalStatus} />
            </CCol>
            <CCol md={3}>
              <div className="text-medium-emphasis small">Active</div>
              <StatusPill value={room.active} />
            </CCol>
          </CRow>
        </CCardBody>
      </CCard>

      <CCard>
        <CCardHeader>
          <CNav variant="tabs" role="tablist">
            <CNavItem>
              <CNavLink active={activeTab === 'general'} onClick={() => setActiveTab('general')}>
                General
              </CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink active={activeTab === 'status'} onClick={() => setActiveTab('status')}>
                Status
              </CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink active={activeTab === 'notes'} onClick={() => setActiveTab('notes')}>
                Notes
              </CNavLink>
            </CNavItem>
          </CNav>
        </CCardHeader>

        <CCardBody>
          <CTabContent>
            <CTabPane visible={activeTab === 'general'}>
              <CForm onSubmit={saveGeneral}>
                <CRow className="g-3">
                  <CCol md={6}>
                    <CFormLabel>Room type</CFormLabel>
                    <CFormSelect
                      value={generalForm.roomTypeId}
                      onChange={(e) =>
                        setGeneralForm({ ...generalForm, roomTypeId: e.target.value })
                      }
                    >
                      <option value="">Select room type</option>
                      {roomTypeOptions.map((type) => (
                        <option key={type.value} value={type.value}>
                          {type.label}
                        </option>
                      ))}
                    </CFormSelect>
                  </CCol>

                  <CCol md={6}>
                    <CFormLabel>Room number</CFormLabel>
                    <CFormInput
                      value={generalForm.roomNumber}
                      onChange={(e) =>
                        setGeneralForm({ ...generalForm, roomNumber: e.target.value })
                      }
                    />
                  </CCol>

                  <CCol md={4}>
                    <CFormLabel>Floor</CFormLabel>
                    <CFormInput
                      type="number"
                      value={generalForm.floor}
                      onChange={(e) => setGeneralForm({ ...generalForm, floor: e.target.value })}
                    />
                  </CCol>

                  <CCol md={4}>
                    <CFormLabel>Capacity</CFormLabel>
                    <CFormInput
                      type="number"
                      value={generalForm.capacity}
                      onChange={(e) =>
                        setGeneralForm({ ...generalForm, capacity: e.target.value })
                      }
                    />
                  </CCol>

                  <CCol md={4}>
                    <CFormLabel>Base price</CFormLabel>
                    <CFormInput
                      type="number"
                      step="0.01"
                      value={generalForm.basePrice}
                      onChange={(e) =>
                        setGeneralForm({ ...generalForm, basePrice: e.target.value })
                      }
                    />
                  </CCol>

                  <CCol md={12}>
                    <CFormLabel>Description</CFormLabel>
                    <CFormInput
                      value={generalForm.description}
                      onChange={(e) =>
                        setGeneralForm({ ...generalForm, description: e.target.value })
                      }
                    />
                  </CCol>

                  <CCol md={12}>
                    <CFormLabel>Notes</CFormLabel>
                    <CFormInput
                      value={generalForm.notes}
                      onChange={(e) => setGeneralForm({ ...generalForm, notes: e.target.value })}
                    />
                  </CCol>

                  <CCol md={4}>
                    <CFormLabel>Active</CFormLabel>
                    <CFormSelect
                      value={String(generalForm.active)}
                      onChange={(e) =>
                        setGeneralForm({
                          ...generalForm,
                          active: e.target.value === 'true',
                        })
                      }
                    >
                      <option value="true">Active</option>
                      <option value="false">Inactive</option>
                    </CFormSelect>
                  </CCol>

                  <CCol xs={12}>
                    <CButton type="submit" color="primary" disabled={saving}>
                      {saving ? 'Saving...' : 'Save changes'}
                    </CButton>
                  </CCol>
                </CRow>
              </CForm>
            </CTabPane>

            <CTabPane visible={activeTab === 'status'}>
              <CRow className="g-4">
                <CCol md={4}>
                  <CCard>
                    <CCardHeader>Occupancy</CCardHeader>
                    <CCardBody>
                      <div className="mb-3">
                        <CFormLabel>Occupancy status</CFormLabel>
                        <CFormSelect
                          value={statusForm.occupancyStatus}
                          onChange={(e) =>
                            setStatusForm({ ...statusForm, occupancyStatus: e.target.value })
                          }
                        >
                          {occupancyOptions.map((value) => (
                            <option key={value} value={value}>
                              {value}
                            </option>
                          ))}
                        </CFormSelect>
                      </div>

                      <CButton color="primary" onClick={saveOccupancy} disabled={saving}>
                        Save occupancy
                      </CButton>
                    </CCardBody>
                  </CCard>
                </CCol>

                <CCol md={4}>
                  <CCard>
                    <CCardHeader>Housekeeping</CCardHeader>
                    <CCardBody>
                      <div className="mb-3">
                        <CFormLabel>Housekeeping status</CFormLabel>
                        <CFormSelect
                          value={statusForm.housekeepingStatus}
                          onChange={(e) =>
                            setStatusForm({ ...statusForm, housekeepingStatus: e.target.value })
                          }
                        >
                          {housekeepingOptions.map((value) => (
                            <option key={value} value={value}>
                              {value}
                            </option>
                          ))}
                        </CFormSelect>
                      </div>

                      <div className="d-flex gap-2 flex-wrap">
                        <CButton color="primary" onClick={saveHousekeeping} disabled={saving}>
                          Save housekeeping
                        </CButton>
                        <CButton color="success" variant="outline" onClick={markClean} disabled={saving}>
                          Mark clean
                        </CButton>
                        <CButton color="warning" variant="outline" onClick={markDirty} disabled={saving}>
                          Mark dirty
                        </CButton>
                      </div>
                    </CCardBody>
                  </CCard>
                </CCol>

                <CCol md={4}>
                  <CCard>
                    <CCardHeader>Technical</CCardHeader>
                    <CCardBody>
                      <div className="mb-3">
                        <CFormLabel>Technical status</CFormLabel>
                        <CFormSelect
                          value={statusForm.technicalStatus}
                          onChange={(e) =>
                            setStatusForm({ ...statusForm, technicalStatus: e.target.value })
                          }
                        >
                          {technicalOptions.map((value) => (
                            <option key={value} value={value}>
                              {value}
                            </option>
                          ))}
                        </CFormSelect>
                      </div>

                      <CButton color="primary" onClick={saveTechnical} disabled={saving}>
                        Save technical
                      </CButton>
                    </CCardBody>
                  </CCard>
                </CCol>

                <CCol xs={12}>
                  <CButton color="danger" variant="outline" onClick={deactivateRoom} disabled={saving}>
                    Deactivate room
                  </CButton>
                </CCol>
              </CRow>
            </CTabPane>

            <CTabPane visible={activeTab === 'notes'}>
              <CCard>
                <CCardBody>
                  <div className="mb-3">
                    <div className="text-medium-emphasis small">Description</div>
                    <div>{room.description || '—'}</div>
                  </div>
                  <div>
                    <div className="text-medium-emphasis small">Notes</div>
                    <div>{room.notes || '—'}</div>
                  </div>
                </CCardBody>
              </CCard>
            </CTabPane>
          </CTabContent>
        </CCardBody>
      </CCard>
    </>
  )
}

export default RoomManage