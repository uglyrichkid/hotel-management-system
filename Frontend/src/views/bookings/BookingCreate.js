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
  CFormTextarea,
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
import { apiFetch, bookingApi, guestApi, roomApi } from '../../services/api'

const createToday = () => new Date().toISOString().split('T')[0]

const createTomorrow = () => {
  const date = new Date()
  date.setDate(date.getDate() + 1)
  return date.toISOString().split('T')[0]
}

const initialSearch = {
  checkInDate: createToday(),
  checkOutDate: createTomorrow(),
  adults: 1,
  children: 0,
}

const initialRoomFilters = {
  roomNumber: '',
  floorFrom: '',
  floorTo: '',
  minCapacity: '',
}

const initialQuickGuestForm = {
  firstName: '',
  lastName: '',
  phone: '',
  email: '',
  documentNumber: '',
}

const badgeColor = (value) => {
  switch (value) {
    case 'ACTIVE':
    case 'CLEAN':
    case 'AVAILABLE':
      return 'success'
    case 'DIRTY':
    case 'TO_CLEAN':
    case 'CLEANING':
    case 'RESERVED':
      return 'warning'
    case 'BLOCKED':
    case 'OUT_OF_SERVICE':
    case 'OUT_OF_ORDER':
    case 'INACTIVE':
    case 'OCCUPIED':
      return 'danger'
    default:
      return 'secondary'
  }
}

const money = (value) => {
  const number = Number(value || 0)
  return number.toFixed(2)
}

const moneyWithCurrency = (value, currencyCode) => {
  const number = Number(value || 0).toFixed(2)
  return currencyCode ? `${number} ${currencyCode}` : number
}

const BookingCreate = () => {
  const navigate = useNavigate()
  const { token, selectedHotelId, selectedHotelName } = useAuth()

  const [searchForm, setSearchForm] = useState(initialSearch)
  const [roomFilters, setRoomFilters] = useState(initialRoomFilters)
  const [availableRooms, setAvailableRooms] = useState([])
  const [selectedRoomIds, setSelectedRoomIds] = useState([])
  const [guests, setGuests] = useState([])
  const [guestQuery, setGuestQuery] = useState('')
  const [selectedGuestId, setSelectedGuestId] = useState('')
  const [notes, setNotes] = useState('')
  const [hotelCurrency, setHotelCurrency] = useState('')
  const [loadingRooms, setLoadingRooms] = useState(false)
  const [loadingGuests, setLoadingGuests] = useState(false)
  const [savingBooking, setSavingBooking] = useState(false)
  const [savingQuickGuest, setSavingQuickGuest] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showQuickGuestModal, setShowQuickGuestModal] = useState(false)
  const [quickGuestForm, setQuickGuestForm] = useState(initialQuickGuestForm)

  const nights = useMemo(() => {
    if (!searchForm.checkInDate || !searchForm.checkOutDate) return 0
    const start = new Date(searchForm.checkInDate)
    const end = new Date(searchForm.checkOutDate)
    const diff = Math.round((end - start) / 86400000)
    return diff > 0 ? diff : 0
  }, [searchForm.checkInDate, searchForm.checkOutDate])

  const selectedGuest = useMemo(
    () => guests.find((guest) => String(guest.id) === String(selectedGuestId)) || null,
    [guests, selectedGuestId],
  )

  const filteredGuests = useMemo(() => {
    if (!guestQuery.trim()) return guests

    const q = guestQuery.trim().toLowerCase()

    return guests.filter((guest) => {
      const text = [
        guest.fullName,
        guest.firstName,
        guest.lastName,
        guest.email,
        guest.phone,
        guest.documentNumber,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()

      return text.includes(q)
    })
  }, [guests, guestQuery])

  const filteredRooms = useMemo(() => {
    return availableRooms.filter((room) => {
      const roomNumberOk = !roomFilters.roomNumber
        ? true
        : String(room.roomNumber || '')
            .toLowerCase()
            .includes(roomFilters.roomNumber.toLowerCase())

      const floorFromOk = !roomFilters.floorFrom
        ? true
        : Number(room.floor || 0) >= Number(roomFilters.floorFrom)

      const floorToOk = !roomFilters.floorTo
        ? true
        : Number(room.floor || 0) <= Number(roomFilters.floorTo)

      const minCapacityOk = !roomFilters.minCapacity
        ? true
        : Number(room.capacity || 0) >= Number(roomFilters.minCapacity)

      return roomNumberOk && floorFromOk && floorToOk && minCapacityOk
    })
  }, [availableRooms, roomFilters])

  const selectedRooms = useMemo(
    () => availableRooms.filter((room) => selectedRoomIds.includes(room.id)),
    [availableRooms, selectedRoomIds],
  )

  const estimatedTotal = useMemo(() => {
    return selectedRooms.reduce((sum, room) => {
      const roomPrice = Number(room.basePrice || 0)
      return sum + roomPrice * nights
    }, 0)
  }, [selectedRooms, nights])

  const totalSelectedCapacity = useMemo(() => {
    return selectedRooms.reduce((sum, room) => sum + Number(room.capacity || 0), 0)
  }, [selectedRooms])

  const totalGuestsCount = Number(searchForm.adults || 0) + Number(searchForm.children || 0)

  const loadGuests = async () => {
    try {
      setLoadingGuests(true)
      const data = await guestApi.getAll({}, token)
      setGuests(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err.message)
      setGuests([])
    } finally {
      setLoadingGuests(false)
    }
  }

  const loadHotelCurrency = async () => {
    if (!selectedHotelId) return

    try {
      const data = await apiFetch(`/api/hotels/${selectedHotelId}/manage`, {}, token)
      setHotelCurrency(data?.currencyCode || '')
    } catch {
      setHotelCurrency('')
    }
  }

  useEffect(() => {
    loadGuests()
  }, [])

  useEffect(() => {
    if (selectedHotelId) {
      loadHotelCurrency()
    }
    setAvailableRooms([])
    setSelectedRoomIds([])
    setSuccess('')
    setError('')
  }, [selectedHotelId])

  const searchRooms = async (event) => {
    event.preventDefault()

    if (!selectedHotelId) {
      setError('Please select a hotel first.')
      return
    }

    try {
      setLoadingRooms(true)
      setError('')
      setSuccess('')
      setSelectedRoomIds([])

      const data = await roomApi.getAvailable(
        {
          hotelId: selectedHotelId,
          checkIn: searchForm.checkInDate,
          checkOut: searchForm.checkOutDate,
        },
        token,
      )

      setAvailableRooms(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err.message)
      setAvailableRooms([])
    } finally {
      setLoadingRooms(false)
    }
  }

  const toggleRoom = (roomId) => {
    setSelectedRoomIds((prev) =>
      prev.includes(roomId) ? prev.filter((id) => id !== roomId) : [...prev, roomId],
    )
  }

  const createQuickGuest = async (event) => {
    event.preventDefault()

    if (!selectedHotelId) {
      setError('Please select a hotel first.')
      return
    }

    try {
      setSavingQuickGuest(true)
      setError('')
      setSuccess('')

      const createdGuest = await bookingApi.quickCreateGuest(selectedHotelId, quickGuestForm, token)

      setShowQuickGuestModal(false)
      setQuickGuestForm(initialQuickGuestForm)

      await loadGuests()

      if (createdGuest?.id) {
        setSelectedGuestId(String(createdGuest.id))
      }

      setSuccess('Guest created successfully.')
    } catch (err) {
      setError(err.message)
    } finally {
      setSavingQuickGuest(false)
    }
  }

  const createBooking = async () => {
    if (!selectedHotelId) {
      setError('Please select a hotel first.')
      return
    }

    if (!selectedGuestId) {
      setError('Please select a guest.')
      return
    }

    if (selectedRoomIds.length === 0) {
      setError('Please select at least one room.')
      return
    }

    try {
      setSavingBooking(true)
      setError('')
      setSuccess('')

      await bookingApi.create(
        selectedHotelId,
        {
          guestId: Number(selectedGuestId),
          roomIds: selectedRoomIds,
          checkInDate: searchForm.checkInDate,
          checkOutDate: searchForm.checkOutDate,
          adults: Number(searchForm.adults || 1),
          children: Number(searchForm.children || 0),
          notes: notes || null,
        },
        token,
      )

      setSuccess('Booking created successfully.')

      setTimeout(() => {
        navigate('/bookings')
      }, 700)
    } catch (err) {
      setError(err.message)
    } finally {
      setSavingBooking(false)
    }
  }

  return (
    <>
      <PageHeader
        title="Create Booking"
        subtitle="Search available rooms, select guest, and create a reservation."
      />

      <SelectedHotelAlert hotelName={selectedHotelName} />

      {error ? <CAlert color="danger">{error}</CAlert> : null}
      {success ? <CAlert color="success">{success}</CAlert> : null}

      <CCard className="mb-4">
        <CCardHeader>Stay details</CCardHeader>
        <CCardBody>
          <CForm onSubmit={searchRooms}>
            <CRow className="g-3 align-items-end">
              <CCol md={3}>
                <CFormLabel>Check-in</CFormLabel>
                <CFormInput
                  type="date"
                  value={searchForm.checkInDate}
                  onChange={(e) =>
                    setSearchForm((prev) => ({ ...prev, checkInDate: e.target.value }))
                  }
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>Check-out</CFormLabel>
                <CFormInput
                  type="date"
                  value={searchForm.checkOutDate}
                  onChange={(e) =>
                    setSearchForm((prev) => ({ ...prev, checkOutDate: e.target.value }))
                  }
                />
              </CCol>

              <CCol md={2}>
                <CFormLabel>Adults</CFormLabel>
                <CFormInput
                  type="number"
                  min={1}
                  value={searchForm.adults}
                  onChange={(e) => setSearchForm((prev) => ({ ...prev, adults: e.target.value }))}
                />
              </CCol>

              <CCol md={2}>
                <CFormLabel>Children</CFormLabel>
                <CFormInput
                  type="number"
                  min={0}
                  value={searchForm.children}
                  onChange={(e) =>
                    setSearchForm((prev) => ({ ...prev, children: e.target.value }))
                  }
                />
              </CCol>

              <CCol md={2}>
                <CButton type="submit" color="primary" className="w-100">
                  {loadingRooms ? <CSpinner size="sm" /> : 'Search rooms'}
                </CButton>
              </CCol>
            </CRow>
          </CForm>
        </CCardBody>
      </CCard>

      <CRow>
        <CCol lg={8}>
          <CCard className="mb-4">
            <CCardHeader>Room filters</CCardHeader>
            <CCardBody>
              <CRow className="g-3">
                <CCol md={4}>
                  <CFormLabel>Room number</CFormLabel>
                  <CFormInput
                    value={roomFilters.roomNumber}
                    onChange={(e) =>
                      setRoomFilters((prev) => ({ ...prev, roomNumber: e.target.value }))
                    }
                  />
                </CCol>

                <CCol md={3}>
                  <CFormLabel>Floor from</CFormLabel>
                  <CFormInput
                    type="number"
                    value={roomFilters.floorFrom}
                    onChange={(e) =>
                      setRoomFilters((prev) => ({ ...prev, floorFrom: e.target.value }))
                    }
                  />
                </CCol>

                <CCol md={3}>
                  <CFormLabel>Floor to</CFormLabel>
                  <CFormInput
                    type="number"
                    value={roomFilters.floorTo}
                    onChange={(e) =>
                      setRoomFilters((prev) => ({ ...prev, floorTo: e.target.value }))
                    }
                  />
                </CCol>

                <CCol md={2}>
                  <CFormLabel>Min capacity</CFormLabel>
                  <CFormInput
                    type="number"
                    value={roomFilters.minCapacity}
                    onChange={(e) =>
                      setRoomFilters((prev) => ({ ...prev, minCapacity: e.target.value }))
                    }
                  />
                </CCol>
              </CRow>
            </CCardBody>
          </CCard>

          <CCard>
            <CCardHeader>Available rooms</CCardHeader>
            <CCardBody>
              {loadingRooms ? <CSpinner color="primary" /> : null}

              <CTable responsive hover>
                <CTableHead>
                  <CTableRow>
                    <CTableHeaderCell>Select</CTableHeaderCell>
                    <CTableHeaderCell>Room</CTableHeaderCell>
                    <CTableHeaderCell>Type</CTableHeaderCell>
                    <CTableHeaderCell>Floor</CTableHeaderCell>
                    <CTableHeaderCell>Capacity</CTableHeaderCell>
                    <CTableHeaderCell>Base price</CTableHeaderCell>
                    <CTableHeaderCell>Housekeeping</CTableHeaderCell>
                    <CTableHeaderCell>Technical</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>

                <CTableBody>
                  {!loadingRooms && filteredRooms.length === 0 ? (
                    <CTableRow>
                      <CTableDataCell colSpan={8} className="text-center text-medium-emphasis">
                        No available rooms found
                      </CTableDataCell>
                    </CTableRow>
                  ) : null}

                  {filteredRooms.map((room) => {
                    const checked = selectedRoomIds.includes(room.id)

                    return (
                      <CTableRow key={room.id}>
                        <CTableDataCell>
                          <CFormCheck
                            checked={checked}
                            onChange={() => toggleRoom(room.id)}
                          />
                        </CTableDataCell>
                        <CTableDataCell>{room.roomNumber}</CTableDataCell>
                        <CTableDataCell>{room.roomTypeName || '—'}</CTableDataCell>
                        <CTableDataCell>{room.floor ?? '—'}</CTableDataCell>
                        <CTableDataCell>{room.capacity ?? '—'}</CTableDataCell>
                        <CTableDataCell>
                          {room.basePrice != null
                            ? moneyWithCurrency(room.basePrice, hotelCurrency)
                            : '—'}
                        </CTableDataCell>
                        <CTableDataCell>
                          <CBadge color={badgeColor(room.housekeepingStatus)}>
                            {room.housekeepingStatus || '—'}
                          </CBadge>
                        </CTableDataCell>
                        <CTableDataCell>
                          <CBadge color={badgeColor(room.technicalStatus)}>
                            {room.technicalStatus || '—'}
                          </CBadge>
                        </CTableDataCell>
                      </CTableRow>
                    )
                  })}
                </CTableBody>
              </CTable>
            </CCardBody>
          </CCard>
        </CCol>

        <CCol lg={4}>
          <CCard className="mb-4">
            <CCardHeader className="d-flex justify-content-between align-items-center">
              <span>Guest</span>
              <CButton size="sm" color="primary" variant="outline" onClick={() => setShowQuickGuestModal(true)}>
                Quick create
              </CButton>
            </CCardHeader>
            <CCardBody>
              <div className="mb-3">
                <CFormLabel>Search guest</CFormLabel>
                <CFormInput
                  placeholder="Search by name, email, phone"
                  value={guestQuery}
                  onChange={(e) => setGuestQuery(e.target.value)}
                />
              </div>

              <div className="mb-3">
                <CFormLabel>Select guest</CFormLabel>
                {loadingGuests ? (
                  <div>
                    <CSpinner size="sm" /> Loading guests...
                  </div>
                ) : (
                  <CFormInput
                    list="guest-options"
                    placeholder="Start typing and then select guest below"
                    value={selectedGuest ? selectedGuest.fullName || '' : ''}
                    readOnly
                  />
                )}
                <datalist id="guest-options">
                  {filteredGuests.map((guest) => (
                    <option
                      key={guest.id}
                      value={`${guest.fullName || ''} ${guest.email || ''} ${guest.phone || ''}`}
                    />
                  ))}
                </datalist>
              </div>

              <div className="border rounded p-2" style={{ maxHeight: 220, overflowY: 'auto' }}>
                {filteredGuests.length === 0 ? (
                  <div className="text-medium-emphasis">No guests found</div>
                ) : (
                  filteredGuests.map((guest) => {
                    const isSelected = String(selectedGuestId) === String(guest.id)

                    return (
                      <div
                        key={guest.id}
                        className={`p-2 rounded mb-2 ${isSelected ? 'bg-light border' : 'border'}`}
                        style={{ cursor: 'pointer' }}
                        onClick={() => setSelectedGuestId(String(guest.id))}
                      >
                        <div><strong>{guest.fullName}</strong></div>
                        <div className="text-medium-emphasis small">{guest.email || '—'}</div>
                        <div className="text-medium-emphasis small">{guest.phone || '—'}</div>
                      </div>
                    )
                  })
                )}
              </div>
            </CCardBody>
          </CCard>

          <CCard>
            <CCardHeader>Booking summary</CCardHeader>
            <CCardBody>
              <div className="mb-3">
                <strong>Hotel:</strong>
                <div>{selectedHotelName || '—'}</div>
              </div>

              <div className="mb-3">
                <strong>Dates:</strong>
                <div>{searchForm.checkInDate} → {searchForm.checkOutDate}</div>
                <div className="text-medium-emphasis">{nights} nights</div>
              </div>

              <div className="mb-3">
                <strong>Guests:</strong>
                <div>{searchForm.adults} adults / {searchForm.children} children</div>
              </div>

              <div className="mb-3">
                <strong>Selected guest:</strong>
                <div>{selectedGuest?.fullName || '—'}</div>
              </div>

              <div className="mb-3">
                <strong>Selected rooms:</strong>
                {selectedRooms.length === 0 ? (
                  <div className="text-medium-emphasis">No rooms selected</div>
                ) : (
                  selectedRooms.map((room) => (
                    <div key={room.id}>
                      #{room.roomNumber} — {room.roomTypeName || 'Room'} — {moneyWithCurrency(room.basePrice, hotelCurrency)}
                    </div>
                  ))
                )}
              </div>

              <div className="mb-3">
                <strong>Total selected capacity:</strong>
                <div>{totalSelectedCapacity}</div>
              </div>

              <div className="mb-3">
                <strong>Estimated total:</strong>
                <div>{moneyWithCurrency(estimatedTotal || 0, hotelCurrency)}</div>
              </div>

              {totalSelectedCapacity > 0 && totalSelectedCapacity < totalGuestsCount ? (
                <CAlert color="warning">
                  Selected rooms capacity is lower than total guests count.
                </CAlert>
              ) : null}

              <div className="mb-3">
                <CFormLabel>Notes</CFormLabel>
                <CFormTextarea
                  rows={4}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Late arrival, quiet room request, extra bed, etc."
                />
              </div>

              <CButton color="success" className="w-100" onClick={createBooking} disabled={savingBooking}>
                {savingBooking ? <CSpinner size="sm" /> : 'Create booking'}
              </CButton>
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>

      <CModal visible={showQuickGuestModal} onClose={() => setShowQuickGuestModal(false)}>
        <CModalHeader>
          <CModalTitle>Quick create guest</CModalTitle>
        </CModalHeader>

        <CForm onSubmit={createQuickGuest}>
          <CModalBody>
            <div className="mb-3">
              <CFormLabel>First name</CFormLabel>
              <CFormInput
                required
                value={quickGuestForm.firstName}
                onChange={(e) =>
                  setQuickGuestForm((prev) => ({ ...prev, firstName: e.target.value }))
                }
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Last name</CFormLabel>
              <CFormInput
                required
                value={quickGuestForm.lastName}
                onChange={(e) =>
                  setQuickGuestForm((prev) => ({ ...prev, lastName: e.target.value }))
                }
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Phone</CFormLabel>
              <CFormInput
                required
                value={quickGuestForm.phone}
                onChange={(e) =>
                  setQuickGuestForm((prev) => ({ ...prev, phone: e.target.value }))
                }
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Email</CFormLabel>
              <CFormInput
                type="email"
                required
                value={quickGuestForm.email}
                onChange={(e) =>
                  setQuickGuestForm((prev) => ({ ...prev, email: e.target.value }))
                }
              />
            </div>

            <div>
              <CFormLabel>Document number</CFormLabel>
              <CFormInput
                value={quickGuestForm.documentNumber}
                onChange={(e) =>
                  setQuickGuestForm((prev) => ({ ...prev, documentNumber: e.target.value }))
                }
              />
            </div>
          </CModalBody>

          <CModalFooter>
            <CButton color="secondary" onClick={() => setShowQuickGuestModal(false)}>
              Cancel
            </CButton>
            <CButton type="submit" color="primary" disabled={savingQuickGuest}>
              {savingQuickGuest ? <CSpinner size="sm" /> : 'Create guest'}
            </CButton>
          </CModalFooter>
        </CForm>
      </CModal>
    </>
  )
}

export default BookingCreate