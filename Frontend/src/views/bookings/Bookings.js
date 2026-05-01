import React, { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  CAlert,
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CForm,
  CFormInput,
  CFormLabel,
  CFormSelect,
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
import StatusBadge from '../../components/common/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { bookingApi } from '../../services/api'

const initialFilters = {
  bookingId: '',
  status: '',
  guestQuery: '',
  paymentState: '',
  roomQuery: '',
  roomTypeQuery: '',
  stayPeriod: '',
  checkInFrom: '',
  checkInTo: '',
  checkOutFrom: '',
  checkOutTo: '',
  createdFrom: '',
  createdTo: '',
  adults: '',
  children: '',
}

const moneyWithCurrency = (value, currencyCode) => {
  const number = Number(value || 0).toFixed(2)
  return currencyCode ? `${number} ${currencyCode}` : number
}

const normalizeDate = (value) => {
  if (!value) return null
  const parsed = new Date(value)
  if (Number.isNaN(parsed.getTime())) return null
  return parsed
}

const formatDateOnly = (value) => {
  const date = normalizeDate(value)
  if (!date) return ''
  return date.toISOString().slice(0, 10)
}

const formatShortDateTime = (value) => {
  const date = normalizeDate(value)
  if (!date) return '—'

  const yyyy = date.getFullYear()
  const mm = `${date.getMonth() + 1}`.padStart(2, '0')
  const dd = `${date.getDate()}`.padStart(2, '0')
  const hh = `${date.getHours()}`.padStart(2, '0')
  const min = `${date.getMinutes()}`.padStart(2, '0')

  return `${yyyy}-${mm}-${dd} ${hh}:${min}`
}

const getTodayDateString = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = `${now.getMonth() + 1}`.padStart(2, '0')
  const day = `${now.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getGuestName = (booking) => {
  if (booking.guestFullName) return booking.guestFullName
  if (booking.guestName) return booking.guestName

  const firstName = booking.guestFirstName || ''
  const lastName = booking.guestLastName || ''
  const fullName = `${firstName} ${lastName}`.trim()

  return fullName || '—'
}

const getRoomsArray = (booking) => {
  if (Array.isArray(booking.rooms)) return booking.rooms
  return []
}

const getRoomNumbersArray = (booking) => {
  if (Array.isArray(booking.roomNumbers)) return booking.roomNumbers
  return []
}

const getRoomsLabel = (booking) => {
  const rooms = getRoomsArray(booking)
  if (rooms.length) {
    return rooms.map((room) => room.roomNumber || room.number || room.id).join(', ')
  }

  const roomNumbers = getRoomNumbersArray(booking)
  if (roomNumbers.length) {
    return roomNumbers.join(', ')
  }

  if (booking.roomsSummary) return booking.roomsSummary
  return '—'
}

const getRoomTypeLabel = (booking) => {
  const rooms = getRoomsArray(booking)

  const roomTypesFromRooms = rooms
    .map((room) => room.roomTypeName || room.roomType || room.typeName || room.type)
    .filter(Boolean)

  if (roomTypesFromRooms.length) {
    return [...new Set(roomTypesFromRooms)].join(', ')
  }

  if (booking.roomTypeName) return booking.roomTypeName
  if (booking.roomType) return booking.roomType
  if (booking.roomTypesSummary) return booking.roomTypesSummary

  return '—'
}

const getCreatedAtLabel = (booking) => {
  return booking.createdAt || booking.created_at || booking.createdDate || '—'
}

const getAdultsValue = (booking) => {
  return booking.adults ?? booking.adultCount ?? ''
}

const getChildrenValue = (booking) => {
  return booking.children ?? booking.childrenCount ?? ''
}

const getRemainingAmount = (booking) => {
  if (booking.status === 'CANCELLED') return 0

  const total = Number(booking.totalPrice || 0)
  const paid = Number(booking.paidAmount || 0)
  return Math.max(total - paid, 0)
}

const matchesPaymentState = (booking, paymentState) => {
  if (!paymentState) return true

  const total = Number(booking.totalPrice || 0)
  const paid = Number(booking.paidAmount || 0)
  const remaining = getRemainingAmount(booking)

  if (paymentState === 'PAID') {
    return total > 0 && remaining === 0
  }

  if (paymentState === 'PARTIALLY_PAID') {
    return paid > 0 && remaining > 0
  }

  if (paymentState === 'TO_BE_PAID') {
    return remaining > 0 && paid === 0
  }

  return true
}

const matchesBookingId = (booking, bookingId) => {
  if (!bookingId) return true
  return String(booking.id || '').includes(String(bookingId).trim())
}

const matchesRoomQuery = (booking, roomQuery) => {
  if (!roomQuery) return true
  return getRoomsLabel(booking).toLowerCase().includes(roomQuery.trim().toLowerCase())
}

const matchesRoomTypeQuery = (booking, roomTypeQuery) => {
  if (!roomTypeQuery) return true
  return getRoomTypeLabel(booking).toLowerCase().includes(roomTypeQuery.trim().toLowerCase())
}

const matchesStayPeriod = (booking, stayPeriod) => {
  if (!stayPeriod) return true

  const today = getTodayDateString()
  const checkIn = formatDateOnly(booking.checkInDate)
  const checkOut = formatDateOnly(booking.checkOutDate)

  if (!checkIn || !checkOut) return false

  if (stayPeriod === 'CURRENT') return checkIn <= today && checkOut >= today
  if (stayPeriod === 'UPCOMING') return checkIn > today
  if (stayPeriod === 'PAST') return checkOut < today

  return true
}

const matchesDateRange = (value, from, to) => {
  if (!from && !to) return true

  const normalized = formatDateOnly(value)
  if (!normalized) return false

  if (from && normalized < from) return false
  if (to && normalized > to) return false

  return true
}

const matchesNumericFilter = (value, expected) => {
  if (expected === '' || expected === null || expected === undefined) return true
  if (value === '' || value === null || value === undefined) return false
  return Number(value) === Number(expected)
}

const Bookings = () => {
  const navigate = useNavigate()
  const { token, selectedHotelId, selectedHotelName } = useAuth()

  const [bookings, setBookings] = useState([])
  const [filters, setFilters] = useState(initialFilters)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const loadBookings = async (nextFilters = filters) => {
    if (!selectedHotelId) return

    try {
      setLoading(true)
      setError('')

      const data = await bookingApi.getAllByHotel(
        selectedHotelId,
        {
          status: nextFilters.status || undefined,
          guestQuery: nextFilters.guestQuery || undefined,
        },
        token,
      )

      setBookings(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err.message)
      setBookings([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!selectedHotelId) return
    setFilters(initialFilters)
    loadBookings(initialFilters)
  }, [selectedHotelId])

  const submitFilters = async (event) => {
    event.preventDefault()
    await loadBookings(filters)
  }

  const resetFilters = async () => {
    setFilters(initialFilters)
    await loadBookings(initialFilters)
  }

  const runAction = async (bookingId, actionKey) => {
    if (!selectedHotelId) return

    try {
      setError('')

      if (actionKey === 'confirm') {
        await bookingApi.confirm(selectedHotelId, bookingId, token)
      }

      if (actionKey === 'check-in') {
        await bookingApi.checkIn(selectedHotelId, bookingId, token)
      }

      if (actionKey === 'check-out') {
        await bookingApi.checkOut(selectedHotelId, bookingId, token)
      }

      if (actionKey === 'cancel') {
        await bookingApi.cancel(selectedHotelId, bookingId, token)
      }

      await loadBookings(filters)
    } catch (err) {
      setError(err.message)
    }
  }

  const filteredBookings = useMemo(() => {
    return bookings.filter((booking) => {
      return (
        matchesBookingId(booking, filters.bookingId) &&
        matchesPaymentState(booking, filters.paymentState) &&
        matchesRoomQuery(booking, filters.roomQuery) &&
        matchesRoomTypeQuery(booking, filters.roomTypeQuery) &&
        matchesStayPeriod(booking, filters.stayPeriod) &&
        matchesDateRange(booking.checkInDate, filters.checkInFrom, filters.checkInTo) &&
        matchesDateRange(booking.checkOutDate, filters.checkOutFrom, filters.checkOutTo) &&
        matchesDateRange(getCreatedAtLabel(booking), filters.createdFrom, filters.createdTo) &&
        matchesNumericFilter(getAdultsValue(booking), filters.adults) &&
        matchesNumericFilter(getChildrenValue(booking), filters.children)
      )
    })
  }, [bookings, filters])

  if (!selectedHotelId) {
    return (
      <>
        <PageHeader
          title="Bookings"
          subtitle="Operational reservation list with filters and lifecycle actions."
          actionLabel="Add new booking"
          onAction={() => navigate('/bookings/create')}
        />
        <SelectedHotelAlert hotelName={selectedHotelName} />
      </>
    )
  }

  return (
    <>
      <PageHeader
        title="Bookings"
        subtitle="Operational reservation list with lifecycle actions, payment visibility and direct access to booking details."
        actionLabel="Add new booking"
        onAction={() => navigate('/bookings/create')}
      />

      {error ? <CAlert color="danger">{error}</CAlert> : null}

      <CCard className="mb-4" style={{ width: '100%' }}>
        <CCardHeader>Booking filters</CCardHeader>
        <CCardBody>
          <CForm onSubmit={submitFilters}>
            <CRow className="g-3 align-items-end mb-3">
              <CCol md={2}>
                <CFormLabel>Booking ID</CFormLabel>
                <CFormInput
                  placeholder="e.g. 15"
                  value={filters.bookingId}
                  onChange={(e) => setFilters((prev) => ({ ...prev, bookingId: e.target.value }))}
                />
              </CCol>

              <CCol md={2}>
                <CFormLabel>Status</CFormLabel>
                <CFormSelect
                  value={filters.status}
                  onChange={(e) => setFilters((prev) => ({ ...prev, status: e.target.value }))}
                >
                  <option value="">All statuses</option>
                  <option value="CREATED">CREATED</option>
                  <option value="CONFIRMED">CONFIRMED</option>
                  <option value="CHECKED_IN">CHECKED_IN</option>
                  <option value="CHECKED_OUT">CHECKED_OUT</option>
                  <option value="CANCELLED">CANCELLED</option>
                </CFormSelect>
              </CCol>

              <CCol md={2}>
                <CFormLabel>Payment state</CFormLabel>
                <CFormSelect
                  value={filters.paymentState}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, paymentState: e.target.value }))
                  }
                >
                  <option value="">All payments</option>
                  <option value="PAID">Paid</option>
                  <option value="TO_BE_PAID">Need payment</option>
                  <option value="PARTIALLY_PAID">Partially paid</option>
                </CFormSelect>
              </CCol>

              <CCol md={3}>
                <CFormLabel>Guest search</CFormLabel>
                <CFormInput
                  placeholder="Name, email, phone"
                  value={filters.guestQuery}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, guestQuery: e.target.value }))
                  }
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>Room search</CFormLabel>
                <CFormInput
                  placeholder="Room number"
                  value={filters.roomQuery}
                  onChange={(e) => setFilters((prev) => ({ ...prev, roomQuery: e.target.value }))}
                />
              </CCol>
            </CRow>

            <CRow className="g-3 align-items-end mb-3">
              <CCol md={3}>
                <CFormLabel>Room type</CFormLabel>
                <CFormInput
                  placeholder="Deluxe, Standard..."
                  value={filters.roomTypeQuery}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, roomTypeQuery: e.target.value }))
                  }
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>Stay period</CFormLabel>
                <CFormSelect
                  value={filters.stayPeriod}
                  onChange={(e) => setFilters((prev) => ({ ...prev, stayPeriod: e.target.value }))}
                >
                  <option value="">All periods</option>
                  <option value="CURRENT">Staying now</option>
                  <option value="UPCOMING">Upcoming</option>
                  <option value="PAST">Past</option>
                </CFormSelect>
              </CCol>

              <CCol md={2}>
                <CFormLabel>Adults</CFormLabel>
                <CFormInput
                  type="number"
                  min="0"
                  placeholder="Any"
                  value={filters.adults}
                  onChange={(e) => setFilters((prev) => ({ ...prev, adults: e.target.value }))}
                />
              </CCol>

              <CCol md={2}>
                <CFormLabel>Children</CFormLabel>
                <CFormInput
                  type="number"
                  min="0"
                  placeholder="Any"
                  value={filters.children}
                  onChange={(e) => setFilters((prev) => ({ ...prev, children: e.target.value }))}
                />
              </CCol>

              <CCol md={2} className="d-flex gap-2">
                <CButton type="submit" color="primary">
                  Apply
                </CButton>
                <CButton type="button" color="secondary" variant="outline" onClick={resetFilters}>
                  Reset
                </CButton>
              </CCol>
            </CRow>

            <CRow className="g-3 align-items-end mb-3">
              <CCol md={3}>
                <CFormLabel>Check-in from</CFormLabel>
                <CFormInput
                  type="date"
                  value={filters.checkInFrom}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, checkInFrom: e.target.value }))
                  }
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>Check-in to</CFormLabel>
                <CFormInput
                  type="date"
                  value={filters.checkInTo}
                  onChange={(e) => setFilters((prev) => ({ ...prev, checkInTo: e.target.value }))}
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>Check-out from</CFormLabel>
                <CFormInput
                  type="date"
                  value={filters.checkOutFrom}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, checkOutFrom: e.target.value }))
                  }
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>Check-out to</CFormLabel>
                <CFormInput
                  type="date"
                  value={filters.checkOutTo}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, checkOutTo: e.target.value }))
                  }
                />
              </CCol>
            </CRow>

            <CRow className="g-3 align-items-end">
              <CCol md={3}>
                <CFormLabel>Created from</CFormLabel>
                <CFormInput
                  type="date"
                  value={filters.createdFrom}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, createdFrom: e.target.value }))
                  }
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>Created to</CFormLabel>
                <CFormInput
                  type="date"
                  value={filters.createdTo}
                  onChange={(e) => setFilters((prev) => ({ ...prev, createdTo: e.target.value }))}
                />
              </CCol>
            </CRow>
          </CForm>
        </CCardBody>
      </CCard>

      <CCard>
        <CCardHeader>Bookings list</CCardHeader>
        <CCardBody style={{ padding: 0 }}>
          {loading ? (
            <div style={{ padding: '1rem' }}>
              <CSpinner color="primary" />
            </div>
          ) : null}

          <div style={{ width: '100%', overflowX: 'auto' }}>
            <CTable
              hover
              align="middle"
              small
              style={{
                width: '100%',
                minWidth: '1600px',
                tableLayout: 'fixed',
                marginBottom: 0,
              }}
            >
              <colgroup>
                <col style={{ width: '55px' }} />    {/* ID */}
                <col style={{ width: '160px' }} />   {/* Guest */}
                <col style={{ width: '110px' }} />   {/* Dates */}
                <col style={{ width: '70px' }} />    {/* Rooms */}
                <col style={{ width: '100px' }} />   {/* Room type */}
                <col style={{ width: '110px' }} />   {/* Guests */}
                <col style={{ width: '130px' }} />   {/* Created */}
                <col style={{ width: '130px' }} />   {/* Total */}
                <col style={{ width: '120px' }} />   {/* Paid */}
                <col style={{ width: '120px' }} />   {/* Remaining */}
                <col style={{ width: '115px' }} />   {/* Payment */}
                <col style={{ width: '105px' }} />   {/* Status */}
                <col style={{ width: '200px' }} />   {/* Actions */}
              </colgroup>

              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    ID
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    Guest
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    Dates
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    Rooms
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    Room type
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    Guests
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    Created
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    Total
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    Paid
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    Remaining
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', textAlign: 'center' }}>
                    Payment
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', textAlign: 'center' }}>
                    Status
                  </CTableHeaderCell>
                  <CTableHeaderCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    Actions
                  </CTableHeaderCell>
                </CTableRow>
              </CTableHead>

              <CTableBody>
                {filteredBookings.map((booking) => {
                  const total = Number(booking.totalPrice || 0)
                  const paid = Number(booking.paidAmount || 0)
                  const remaining = getRemainingAmount(booking)
                  const adults = getAdultsValue(booking)
                  const children = getChildrenValue(booking)

                  return (
                    <CTableRow
                      key={booking.id}
                      style={{ cursor: 'pointer' }}
                      onClick={() => navigate(`/bookings/${booking.id}`)}
                    >
                      <CTableDataCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', verticalAlign: 'middle' }}>
                        #{booking.id}
                      </CTableDataCell>

                      <CTableDataCell style={{ overflow: 'hidden', verticalAlign: 'middle' }}>
                        <div
                          className="fw-semibold"
                          style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                          title={getGuestName(booking)}
                        >
                          {getGuestName(booking)}
                        </div>
                      </CTableDataCell>

                      <CTableDataCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', verticalAlign: 'middle' }}>
                        <div>{booking.checkInDate || '—'}</div>
                        <div className="text-body-secondary small">{booking.checkOutDate || '—'}</div>
                      </CTableDataCell>

                      <CTableDataCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', verticalAlign: 'middle' }}>
                        {getRoomsLabel(booking)}
                      </CTableDataCell>

                      <CTableDataCell style={{ overflow: 'hidden', verticalAlign: 'middle' }}>
                        <span
                          className="small"
                          style={{ display: 'block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                          title={getRoomTypeLabel(booking)}
                        >
                          {getRoomTypeLabel(booking)}
                        </span>
                      </CTableDataCell>

                      <CTableDataCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', verticalAlign: 'middle' }}>
                        <div>Adults: {adults === '' ? '—' : adults}</div>
                        <div className="small text-body-secondary">
                          Children: {children === '' ? '—' : children}
                        </div>
                      </CTableDataCell>

                      <CTableDataCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', verticalAlign: 'middle' }}>
                        <span className="small">{formatShortDateTime(getCreatedAtLabel(booking))}</span>
                      </CTableDataCell>

                      <CTableDataCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', verticalAlign: 'middle' }}>
                        <div className="fw-semibold">{moneyWithCurrency(total, booking.currencyCode)}</div>
                      </CTableDataCell>

                      <CTableDataCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', verticalAlign: 'middle' }}>
                        <div className={paid > 0 ? 'text-success fw-semibold' : 'text-body-secondary'}>
                          {moneyWithCurrency(paid, booking.currencyCode)}
                        </div>
                      </CTableDataCell>

                      <CTableDataCell style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', verticalAlign: 'middle' }}>
                        <div className={remaining > 0 ? 'text-danger fw-semibold' : 'text-success fw-semibold'}>
                          {moneyWithCurrency(remaining, booking.currencyCode)}
                        </div>
                      </CTableDataCell>

                      <CTableDataCell style={{ verticalAlign: 'middle', textAlign: 'center' }}>
                        <StatusBadge value={booking.paymentStatus} />
                      </CTableDataCell>

                      <CTableDataCell style={{ verticalAlign: 'middle', textAlign: 'center' }}>
                        <StatusBadge value={booking.status} />
                      </CTableDataCell>

                      {/* ✅ FIXED: Actions — все кнопки горизонтально в одну строку */}
                      <CTableDataCell
                        onClick={(e) => e.stopPropagation()}
                        style={{ verticalAlign: 'middle' }}
                      >
                        <div
                          style={{
                            display: 'flex',
                            flexDirection: 'row',
                            flexWrap: 'nowrap',
                            alignItems: 'center',
                            gap: '4px',
                          }}
                        >
                          <CButton
                            size="sm"
                            color="primary"
                            variant="outline"
                            style={{ whiteSpace: 'nowrap', flexShrink: 0 }}
                            onClick={() => navigate(`/bookings/${booking.id}`)}
                          >
                            Open
                          </CButton>

                          {booking.status === 'CREATED' && (
                            <CButton
                              size="sm"
                              color="info"
                              variant="outline"
                              style={{ whiteSpace: 'nowrap', flexShrink: 0 }}
                              onClick={() => runAction(booking.id, 'confirm')}
                            >
                              Confirm
                            </CButton>
                          )}

                          {booking.status === 'CONFIRMED' && (
                            <CButton
                              size="sm"
                              color="primary"
                              variant="outline"
                              style={{ whiteSpace: 'nowrap', flexShrink: 0 }}
                              onClick={() => runAction(booking.id, 'check-in')}
                            >
                              Check-in
                            </CButton>
                          )}

                          {booking.status === 'CHECKED_IN' && (
                            <CButton
                              size="sm"
                              color="success"
                              variant="outline"
                              style={{ whiteSpace: 'nowrap', flexShrink: 0 }}
                              onClick={() => runAction(booking.id, 'check-out')}
                            >
                              Check-out
                            </CButton>
                          )}

                          {booking.status !== 'CANCELLED' && booking.status !== 'CHECKED_OUT' && (
                            <CButton
                              size="sm"
                              color="danger"
                              variant="outline"
                              style={{ whiteSpace: 'nowrap', flexShrink: 0 }}
                              onClick={() => runAction(booking.id, 'cancel')}
                            >
                              Cancel
                            </CButton>
                          )}
                        </div>
                      </CTableDataCell>
                    </CTableRow>
                  )
                })}

                {!loading && filteredBookings.length === 0 ? (
                  <CTableRow>
                    <CTableDataCell colSpan={13} className="text-center text-body-secondary py-4">
                      No bookings found for current filters.
                    </CTableDataCell>
                  </CTableRow>
                ) : null}
              </CTableBody>
            </CTable>
          </div>
        </CCardBody>
      </CCard>
    </>
  )
}

export default Bookings