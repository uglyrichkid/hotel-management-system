import React, { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  CAlert,
  CBadge,
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CForm,
  CFormCheck,
  CFormInput,
  CFormLabel,
  CFormSelect,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
  CRow,
  CSpinner,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
} from '@coreui/react'
import PageHeader from '../../components/common/PageHeader'
import SelectedHotelAlert from '../../components/common/SelectedHotelAlert'
import { useAuth } from '../../context/AuthContext'
import { apiFetch, buildQuery } from '../../services/api'

const initialFilters = {
  roomNumber: '',
  roomTypeId: '',
  occupancyStatus: '',
  housekeepingStatus: '',
  technicalStatus: '',
  floorFrom: '',
  floorTo: '',
  active: 'true',
  capacity: '',
  onlyAvailable: false,
  checkIn: '',
  checkOut: '',
}

const initialForm = {
  roomTypeId: '',
  roomNumber: '',
  floor: '',
  capacity: '',
  basePrice: '',
  description: '',
  notes: '',
}

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

const Rooms = () => {
  const navigate = useNavigate()
  const { token, selectedHotelId, selectedHotelName } = useAuth()

  const [rooms, setRooms] = useState([])
  const [roomTypes, setRoomTypes] = useState([])
  const [hotelCurrency, setHotelCurrency] = useState('')
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [filters, setFilters] = useState(initialFilters)
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState(initialForm)

  const roomTypeOptions = useMemo(
    () => roomTypes.map((type) => ({ value: String(type.id), label: type.name })),
    [roomTypes],
  )

  const loadRoomTypes = async () => {
    try {
      const data = await apiFetch('/api/room-types', {}, token)
      setRoomTypes(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err.message)
    }
  }

  const loadHotelCurrency = async () => {
    if (!selectedHotelId) return

    try {
      const data = await apiFetch(`/api/hotels/${selectedHotelId}/manage`, {}, token)
      setHotelCurrency(data?.currencyCode || '')
    } catch (err) {
      setError(err.message)
      setHotelCurrency('')
    }
  }

  const loadRooms = async (nextFilters = filters) => {
    if (!selectedHotelId) return

    try {
      setLoading(true)
      setError('')

      const query = buildQuery({
        hotelId: selectedHotelId,
        roomNumber: nextFilters.roomNumber,
        roomTypeId: nextFilters.roomTypeId || undefined,
        occupancyStatus: nextFilters.occupancyStatus,
        housekeepingStatus: nextFilters.housekeepingStatus,
        technicalStatus: nextFilters.technicalStatus,
        floorFrom: nextFilters.floorFrom || undefined,
        floorTo: nextFilters.floorTo || undefined,
        active:
          nextFilters.active === ''
            ? undefined
            : nextFilters.active === 'true'
              ? true
              : false,
        capacity: nextFilters.capacity || undefined,
        onlyAvailable: nextFilters.onlyAvailable,
        checkIn: nextFilters.checkIn,
        checkOut: nextFilters.checkOut,
      })

      const data = await apiFetch(`/api/rooms${query}`, {}, token)
      setRooms(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err.message)
      setRooms([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRoomTypes()
  }, [])

  useEffect(() => {
    if (!selectedHotelId) return

    const init = async () => {
      setFilters(initialFilters)
      await Promise.all([
        loadRooms(initialFilters),
        loadHotelCurrency(),
      ])
    }

    init()
  }, [selectedHotelId])

  const submitFilters = async (event) => {
    event.preventDefault()
    await loadRooms(filters)
  }

  const resetFilters = async () => {
    setFilters(initialFilters)
    await loadRooms(initialFilters)
  }

  const createRoom = async (event) => {
    event.preventDefault()
    if (!selectedHotelId) return

    try {
      setSaving(true)
      setError('')

      await apiFetch(
        '/api/rooms',
        {
          method: 'POST',
          body: JSON.stringify({
            hotelId: Number(selectedHotelId),
            roomTypeId: Number(form.roomTypeId),
            roomNumber: form.roomNumber.trim(),
            floor: form.floor ? Number(form.floor) : null,
            capacity: form.capacity ? Number(form.capacity) : null,
            basePrice: form.basePrice ? Number(form.basePrice) : null,
            description: form.description || null,
            notes: form.notes || null,
          }),
        },
        token,
      )

      setForm(initialForm)
      setShowModal(false)
      await loadRooms()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const markClean = async (roomId) => {
    try {
      await apiFetch(`/api/rooms/${roomId}/mark-clean`, { method: 'PATCH' }, token)
      await loadRooms()
    } catch (err) {
      setError(err.message)
    }
  }

  const markDirty = async (roomId) => {
    try {
      await apiFetch(`/api/rooms/${roomId}/mark-dirty`, { method: 'PATCH' }, token)
      await loadRooms()
    } catch (err) {
      setError(err.message)
    }
  }

  const deactivateRoom = async (roomId) => {
    if (!window.confirm('Deactivate this room?')) return

    try {
      await apiFetch(`/api/rooms/${roomId}`, { method: 'DELETE' }, token)
      await loadRooms()
    } catch (err) {
      setError(err.message)
    }
  }

  const changeOccupancy = async (roomId, occupancyStatus) => {
    try {
      await apiFetch(
        `/api/rooms/${roomId}/occupancy`,
        {
          method: 'PATCH',
          body: JSON.stringify({ occupancyStatus }),
        },
        token,
      )
      await loadRooms()
    } catch (err) {
      setError(err.message)
    }
  }

  const changeTechnical = async (roomId, technicalStatus) => {
    try {
      await apiFetch(
        `/api/rooms/${roomId}/technical`,
        {
          method: 'PATCH',
          body: JSON.stringify({ technicalStatus }),
        },
        token,
      )
      await loadRooms()
    } catch (err) {
      setError(err.message)
    }
  }

  if (!selectedHotelId) {
    return (
      <>
        <PageHeader
          title="Rooms"
          subtitle="Manage room inventory, statuses, pricing, and availability."
        />
        <SelectedHotelAlert />
      </>
    )
  }

  return (
    <>
      <PageHeader
        title="Rooms"
        subtitle={
          selectedHotelName
            ? `${selectedHotelName} — room inventory and operational status management.`
            : `Hotel #${selectedHotelId} — room inventory and operational status management.`
        }
        actionLabel="Add room"
        onAction={() => setShowModal(true)}
      />

      {error ? <CAlert color="danger">{error}</CAlert> : null}

      <CCard className="mb-4">
        <CCardHeader>Filters</CCardHeader>
        <CCardBody>
          <CForm onSubmit={submitFilters}>
            <CRow className="g-3">
              <CCol md={3}>
                <CFormLabel>Room number</CFormLabel>
                <CFormInput
                  value={filters.roomNumber}
                  onChange={(e) => setFilters({ ...filters, roomNumber: e.target.value })}
                  placeholder="101"
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>Room type</CFormLabel>
                <CFormSelect
                  value={filters.roomTypeId}
                  onChange={(e) => setFilters({ ...filters, roomTypeId: e.target.value })}
                >
                  <option value="">All</option>
                  {roomTypeOptions.map((type) => (
                    <option key={type.value} value={type.value}>
                      {type.label}
                    </option>
                  ))}
                </CFormSelect>
              </CCol>

              <CCol md={2}>
                <CFormLabel>Occupancy</CFormLabel>
                <CFormSelect
                  value={filters.occupancyStatus}
                  onChange={(e) => setFilters({ ...filters, occupancyStatus: e.target.value })}
                >
                  <option value="">All</option>
                  {occupancyOptions.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </CFormSelect>
              </CCol>

              <CCol md={2}>
                <CFormLabel>Housekeeping</CFormLabel>
                <CFormSelect
                  value={filters.housekeepingStatus}
                  onChange={(e) => setFilters({ ...filters, housekeepingStatus: e.target.value })}
                >
                  <option value="">All</option>
                  {housekeepingOptions.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </CFormSelect>
              </CCol>

              <CCol md={2}>
                <CFormLabel>Technical</CFormLabel>
                <CFormSelect
                  value={filters.technicalStatus}
                  onChange={(e) => setFilters({ ...filters, technicalStatus: e.target.value })}
                >
                  <option value="">All</option>
                  {technicalOptions.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </CFormSelect>
              </CCol>

              <CCol md={2}>
                <CFormLabel>Floor from</CFormLabel>
                <CFormInput
                  type="number"
                  value={filters.floorFrom}
                  onChange={(e) => setFilters({ ...filters, floorFrom: e.target.value })}
                />
              </CCol>

              <CCol md={2}>
                <CFormLabel>Floor to</CFormLabel>
                <CFormInput
                  type="number"
                  value={filters.floorTo}
                  onChange={(e) => setFilters({ ...filters, floorTo: e.target.value })}
                />
              </CCol>

              <CCol md={2}>
                <CFormLabel>Capacity</CFormLabel>
                <CFormInput
                  type="number"
                  value={filters.capacity}
                  onChange={(e) => setFilters({ ...filters, capacity: e.target.value })}
                />
              </CCol>

              <CCol md={2}>
                <CFormLabel>Active</CFormLabel>
                <CFormSelect
                  value={filters.active}
                  onChange={(e) => setFilters({ ...filters, active: e.target.value })}
                >
                  <option value="">All</option>
                  <option value="true">Active</option>
                  <option value="false">Inactive</option>
                </CFormSelect>
              </CCol>

              <CCol md={2}>
                <CFormLabel>Check-in</CFormLabel>
                <CFormInput
                  type="date"
                  value={filters.checkIn}
                  onChange={(e) => setFilters({ ...filters, checkIn: e.target.value })}
                />
              </CCol>

              <CCol md={2}>
                <CFormLabel>Check-out</CFormLabel>
                <CFormInput
                  type="date"
                  value={filters.checkOut}
                  onChange={(e) => setFilters({ ...filters, checkOut: e.target.value })}
                />
              </CCol>

              <CCol md={3} className="d-flex align-items-end">
                <CFormCheck
                  label="Only available"
                  checked={filters.onlyAvailable}
                  onChange={(e) => setFilters({ ...filters, onlyAvailable: e.target.checked })}
                />
              </CCol>

              <CCol xs={12} className="d-flex gap-2">
                <CButton type="submit" color="primary">
                  Apply filters
                </CButton>
                <CButton type="button" color="secondary" variant="outline" onClick={resetFilters}>
                  Reset
                </CButton>
              </CCol>
            </CRow>
          </CForm>
        </CCardBody>
      </CCard>

      <CCard>
        <CCardHeader>Rooms</CCardHeader>
        <CCardBody>
          {loading ? <CSpinner color="primary" /> : null}

          {!loading && rooms.length === 0 ? (
            <CAlert color="light" className="mb-0">
              No rooms found for the selected filters.
            </CAlert>
          ) : null}

          {!loading && rooms.length > 0 ? (
            <CTable responsive hover align="middle">
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>Room Number</CTableHeaderCell>
                  <CTableHeaderCell>Type</CTableHeaderCell>
                  <CTableHeaderCell>Floor</CTableHeaderCell>
                  <CTableHeaderCell>Capacity</CTableHeaderCell>
                  <CTableHeaderCell>Price</CTableHeaderCell>
                  <CTableHeaderCell>Occupancy</CTableHeaderCell>
                  <CTableHeaderCell>Housekeeping</CTableHeaderCell>
                  <CTableHeaderCell>Technical</CTableHeaderCell>
                  <CTableHeaderCell>Active</CTableHeaderCell>
                  <CTableHeaderCell>Actions</CTableHeaderCell>
                </CTableRow>
              </CTableHead>

              <CTableBody>
                {rooms.map((room) => (
                  <CTableRow key={room.id}>
                    <CTableDataCell>{room.roomNumber}</CTableDataCell>
                    <CTableDataCell>{room.roomTypeName || '—'}</CTableDataCell>
                    <CTableDataCell>{room.floor ?? '—'}</CTableDataCell>
                    <CTableDataCell>{room.capacity ?? '—'}</CTableDataCell>
                    <CTableDataCell>
                      {room.basePrice != null
                        ? `${room.basePrice} ${hotelCurrency || ''}`.trim()
                        : '—'}
                    </CTableDataCell>
                    <CTableDataCell>
                      <StatusPill value={room.occupancyStatus} />
                    </CTableDataCell>
                    <CTableDataCell>
                      <StatusPill value={room.housekeepingStatus} />
                    </CTableDataCell>
                    <CTableDataCell>
                      <StatusPill value={room.technicalStatus} />
                    </CTableDataCell>
                    <CTableDataCell>
                      <StatusPill value={room.active} />
                    </CTableDataCell>

                    <CTableDataCell style={{ minWidth: '260px' }}>
                      <div className="d-flex flex-column gap-2">
                        <div className="d-flex flex-wrap gap-2">
                          <CButton
                            size="sm"
                            color="success"
                            variant="outline"
                            onClick={() => markClean(room.id)}
                          >
                            Clean
                          </CButton>

                          <CButton
                            size="sm"
                            color="warning"
                            variant="outline"
                            onClick={() => markDirty(room.id)}
                          >
                            Dirty
                          </CButton>

                          <CButton
                            size="sm"
                            color="info"
                            variant="outline"
                            onClick={() => changeOccupancy(room.id, 'BLOCKED')}
                          >
                            Block
                          </CButton>
                        </div>

                        <div className="d-flex flex-wrap gap-2">
                          <CButton
                            size="sm"
                            color="secondary"
                            variant="outline"
                            onClick={() => changeTechnical(room.id, 'ACTIVE')}
                          >
                            Tech active
                          </CButton>

                          <CButton
                            size="sm"
                            color="danger"
                            variant="outline"
                            onClick={() => deactivateRoom(room.id)}
                          >
                            Deactivate
                          </CButton>

                          <CButton
                            size="sm"
                            color="primary"
                            variant="outline"
                            onClick={() => navigate(`/rooms/${room.id}`)}
                          >
                            Manage
                          </CButton>
                        </div>
                      </div>
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          ) : null}
        </CCardBody>
      </CCard>

      <CModal visible={showModal} onClose={() => setShowModal(false)}>
        <CModalHeader>
          <CModalTitle>Add room</CModalTitle>
        </CModalHeader>

        <CForm onSubmit={createRoom}>
          <CModalBody>
            <div className="mb-3">
              <CFormLabel>Room type</CFormLabel>
              <CFormSelect
                required
                value={form.roomTypeId}
                onChange={(e) => setForm({ ...form, roomTypeId: e.target.value })}
              >
                <option value="">Select room type</option>
                {roomTypeOptions.map((type) => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </CFormSelect>
            </div>

            <div className="mb-3">
              <CFormLabel>Room number</CFormLabel>
              <CFormInput
                required
                value={form.roomNumber}
                onChange={(e) => setForm({ ...form, roomNumber: e.target.value })}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Floor</CFormLabel>
              <CFormInput
                type="number"
                value={form.floor}
                onChange={(e) => setForm({ ...form, floor: e.target.value })}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Capacity</CFormLabel>
              <CFormInput
                type="number"
                required
                value={form.capacity}
                onChange={(e) => setForm({ ...form, capacity: e.target.value })}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>
                Base price {hotelCurrency ? `(${hotelCurrency})` : ''}
              </CFormLabel>
              <CFormInput
                type="number"
                step="0.01"
                required
                value={form.basePrice}
                onChange={(e) => setForm({ ...form, basePrice: e.target.value })}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Description</CFormLabel>
              <CFormInput
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
              />
            </div>

            <div>
              <CFormLabel>Notes</CFormLabel>
              <CFormInput
                value={form.notes}
                onChange={(e) => setForm({ ...form, notes: e.target.value })}
              />
            </div>
          </CModalBody>

          <CModalFooter>
            <CButton color="secondary" onClick={() => setShowModal(false)}>
              Cancel
            </CButton>
            <CButton type="submit" color="primary" disabled={saving}>
              {saving ? 'Saving...' : 'Create'}
            </CButton>
          </CModalFooter>
        </CForm>
      </CModal>
    </>
  )
}

export default Rooms