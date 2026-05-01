import React, { useEffect, useMemo, useState } from 'react'
import {
  CAlert,
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
  CWidgetStatsF,
} from '@coreui/react'
import PageHeader from '../../components/common/PageHeader'
import SelectedHotelAlert from '../../components/common/SelectedHotelAlert'
import StatusBadge from '../../components/common/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { bookingApi } from '../../services/api'

const initialFilters = {
  bookingId: '',
  guestQuery: '',
  method: '',
  status: '',
}

const money = (value) => {
  const number = Number(value || 0)
  return number.toFixed(2)
}

const moneyWithCurrency = (value, currencyCode) => {
  const formatted = money(value)
  return currencyCode ? `${formatted} ${currencyCode}` : formatted
}

const formatDateTime = (value) => {
  if (!value) return '—'
  try {
    return new Date(value).toLocaleString()
  } catch {
    return value
  }
}

const getGuestName = (booking) => {
  if (!booking) return '—'
  if (booking.guestFullName) return booking.guestFullName

  const firstName = booking.guestFirstName || ''
  const lastName = booking.guestLastName || ''
  const fullName = `${firstName} ${lastName}`.trim()

  return fullName || '—'
}

const isToday = (value) => {
  if (!value) return false
  const date = new Date(value)
  const today = new Date()

  return (
    date.getFullYear() === today.getFullYear() &&
    date.getMonth() === today.getMonth() &&
    date.getDate() === today.getDate()
  )
}

const Payments = () => {
  const { token, selectedHotelId, selectedHotelName } = useAuth()

  const [payments, setPayments] = useState([])
  const [bookingsMap, setBookingsMap] = useState({})
  const [filters, setFilters] = useState(initialFilters)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const loadPaymentsRegister = async () => {
    if (!selectedHotelId) return

    try {
      setLoading(true)
      setError('')

      const bookings = await bookingApi.getAllByHotel(selectedHotelId, {}, token)
      const bookingList = Array.isArray(bookings) ? bookings : []

      const paymentsByBooking = await Promise.all(
        bookingList.map(async (booking) => {
          try {
            const bookingPayments = await bookingApi.getPayments(booking.id, token)
            return {
              booking,
              payments: Array.isArray(bookingPayments) ? bookingPayments : [],
            }
          } catch {
            return {
              booking,
              payments: [],
            }
          }
        }),
      )

      const nextBookingsMap = {}
      const flattenedPayments = []

      paymentsByBooking.forEach(({ booking, payments }) => {
        nextBookingsMap[booking.id] = booking

        payments.forEach((payment) => {
          flattenedPayments.push({
            ...payment,
            bookingId: booking.id,
            guestFullName: getGuestName(booking),
            bookingStatus: booking.status,
            bookingPaymentStatus: booking.paymentStatus,
            currencyCode: payment.currencyCode || booking.currencyCode || '',
          })
        })
      })

      flattenedPayments.sort((a, b) => {
        const aTime = new Date(a.paidAt || a.createdAt || 0).getTime()
        const bTime = new Date(b.paidAt || b.createdAt || 0).getTime()
        return bTime - aTime
      })

      setBookingsMap(nextBookingsMap)
      setPayments(flattenedPayments)
    } catch (err) {
      setError(err.message)
      setPayments([])
      setBookingsMap({})
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!selectedHotelId) return
    setFilters(initialFilters)
    loadPaymentsRegister()
  }, [selectedHotelId])

  const filteredPayments = useMemo(() => {
    return payments.filter((payment) => {
      const booking = bookingsMap[payment.bookingId]
      const guestName = (payment.guestFullName || '').toLowerCase()
      const guestEmail = (booking?.guestEmail || '').toLowerCase()
      const guestPhone = (booking?.guestPhone || '').toLowerCase()
      const guestQuery = filters.guestQuery.trim().toLowerCase()

      if (filters.bookingId && String(payment.bookingId) !== String(filters.bookingId)) {
        return false
      }

      if (guestQuery) {
        const matchesGuest =
          guestName.includes(guestQuery) ||
          guestEmail.includes(guestQuery) ||
          guestPhone.includes(guestQuery)

        if (!matchesGuest) return false
      }

      if (filters.method && payment.paymentMethod !== filters.method) {
        return false
      }

      if (filters.status && payment.paymentStatus !== filters.status) {
        return false
      }

      return true
    })
  }, [payments, bookingsMap, filters])

  const hotelCurrency = useMemo(() => {
    const firstWithCurrency = payments.find((payment) => payment.currencyCode)
    return firstWithCurrency?.currencyCode || ''
  }, [payments])

  const summary = useMemo(() => {
    const completed = filteredPayments.filter((payment) => payment.paymentStatus === 'COMPLETED')
    const failed = filteredPayments.filter((payment) => payment.paymentStatus === 'FAILED')
    const todayCompleted = completed.filter((payment) => isToday(payment.paidAt || payment.createdAt))

    const cashToday = todayCompleted
      .filter((payment) => payment.paymentMethod === 'CASH')
      .reduce((sum, payment) => sum + Number(payment.amount || 0), 0)

    const cardToday = todayCompleted
      .filter((payment) => payment.paymentMethod === 'CARD')
      .reduce((sum, payment) => sum + Number(payment.amount || 0), 0)

    const totalToday = todayCompleted.reduce((sum, payment) => sum + Number(payment.amount || 0), 0)

    return {
      totalToday,
      cashToday,
      cardToday,
      failedCount: failed.length,
      currencyCode: hotelCurrency,
    }
  }, [filteredPayments, hotelCurrency])

  const resetFilters = () => {
    setFilters(initialFilters)
  }

  if (!selectedHotelId) {
    return (
      <>
        <PageHeader
          title="Payments register"
          subtitle="Hotel-wide payment journal for the selected hotel context."
        />
        <SelectedHotelAlert hotelName={selectedHotelName} />
      </>
    )
  }

  return (
    <>
      <PageHeader
        title="Payments register"
        subtitle="Hotel-wide payment journal with financial visibility for completed, pending and failed booking payments."
      />

      {error ? <CAlert color="danger">{error}</CAlert> : null}

      <CRow className="g-4 mb-4">
        <CCol sm={6} xl={3}>
          <CWidgetStatsF
            className="h-100"
            color="primary"
            title="Collected today"
            value={moneyWithCurrency(summary.totalToday, summary.currencyCode)}
          />
        </CCol>
        <CCol sm={6} xl={3}>
          <CWidgetStatsF
            className="h-100"
            color="success"
            title="Cash today"
            value={moneyWithCurrency(summary.cashToday, summary.currencyCode)}
          />
        </CCol>
        <CCol sm={6} xl={3}>
          <CWidgetStatsF
            className="h-100"
            color="info"
            title="Card today"
            value={moneyWithCurrency(summary.cardToday, summary.currencyCode)}
          />
        </CCol>
        <CCol sm={6} xl={3}>
          <CWidgetStatsF
            className="h-100"
            color="danger"
            title="Failed payments"
            value={String(summary.failedCount)}
          />
        </CCol>
      </CRow>

      <CCard className="mb-4">
        <CCardHeader>Filters</CCardHeader>
        <CCardBody>
          <CForm>
            <CRow className="g-3 align-items-end">
              <CCol md={3}>
                <CFormLabel>Booking ID</CFormLabel>
                <CFormInput
                  value={filters.bookingId}
                  onChange={(e) => setFilters((prev) => ({ ...prev, bookingId: e.target.value }))}
                  placeholder="e.g. 12"
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>Guest</CFormLabel>
                <CFormInput
                  value={filters.guestQuery}
                  onChange={(e) => setFilters((prev) => ({ ...prev, guestQuery: e.target.value }))}
                  placeholder="Name, email or phone"
                />
              </CCol>

              <CCol md={2}>
                <CFormLabel>Method</CFormLabel>
                <CFormSelect
                  value={filters.method}
                  onChange={(e) => setFilters((prev) => ({ ...prev, method: e.target.value }))}
                >
                  <option value="">All methods</option>
                  <option value="CARD">CARD</option>
                  <option value="CASH">CASH</option>
                  <option value="BANK_TRANSFER">BANK_TRANSFER</option>
                  <option value="ONLINE">ONLINE</option>
                  <option value="POS">POS</option>
                </CFormSelect>
              </CCol>

              <CCol md={2}>
                <CFormLabel>Status</CFormLabel>
                <CFormSelect
                  value={filters.status}
                  onChange={(e) => setFilters((prev) => ({ ...prev, status: e.target.value }))}
                >
                  <option value="">All statuses</option>
                  <option value="PENDING">PENDING</option>
                  <option value="COMPLETED">COMPLETED</option>
                  <option value="FAILED">FAILED</option>
                  <option value="REFUNDED">REFUNDED</option>
                </CFormSelect>
              </CCol>

              <CCol md={2}>
                <div className="d-flex gap-2">
                  <button type="button" className="btn btn-outline-secondary" onClick={resetFilters}>
                    Reset
                  </button>
                  <button type="button" className="btn btn-primary" onClick={loadPaymentsRegister}>
                    Reload
                  </button>
                </div>
              </CCol>
            </CRow>
          </CForm>
        </CCardBody>
      </CCard>

      <CCard>
        <CCardHeader>Payments journal</CCardHeader>
        <CCardBody>
          {loading ? <CSpinner color="primary" /> : null}

          <CTable responsive hover align="middle">
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>Payment ID</CTableHeaderCell>
                <CTableHeaderCell>Booking</CTableHeaderCell>
                <CTableHeaderCell>Guest</CTableHeaderCell>
                <CTableHeaderCell>Amount</CTableHeaderCell>
                <CTableHeaderCell>Method</CTableHeaderCell>
                <CTableHeaderCell>Status</CTableHeaderCell>
                <CTableHeaderCell>Paid at</CTableHeaderCell>
                <CTableHeaderCell>Reference</CTableHeaderCell>
                <CTableHeaderCell>Notes</CTableHeaderCell>
              </CTableRow>
            </CTableHead>

            <CTableBody>
              {filteredPayments.map((payment) => (
                <CTableRow key={payment.id}>
                  <CTableDataCell>#{payment.id}</CTableDataCell>

                  <CTableDataCell>
                    <div className="d-flex flex-column">
                      <span className="fw-semibold">#{payment.bookingId}</span>
                      <span className="small text-body-secondary">
                        <StatusBadge value={payment.bookingStatus} />
                      </span>
                    </div>
                  </CTableDataCell>

                  <CTableDataCell>{payment.guestFullName || '—'}</CTableDataCell>

                  <CTableDataCell>
                    <span className="fw-semibold">
                      {moneyWithCurrency(payment.amount, payment.currencyCode)}
                    </span>
                  </CTableDataCell>

                  <CTableDataCell>
                    <StatusBadge value={payment.paymentMethod} />
                  </CTableDataCell>

                  <CTableDataCell>
                    <StatusBadge value={payment.paymentStatus} />
                  </CTableDataCell>

                  <CTableDataCell>{formatDateTime(payment.paidAt || payment.createdAt)}</CTableDataCell>

                  <CTableDataCell>{payment.transactionReference || '—'}</CTableDataCell>

                  <CTableDataCell>{payment.notes || '—'}</CTableDataCell>
                </CTableRow>
              ))}

              {!loading && filteredPayments.length === 0 ? (
                <CTableRow>
                  <CTableDataCell colSpan={9} className="text-center text-body-secondary py-4">
                    No payments found for current hotel and filters.
                  </CTableDataCell>
                </CTableRow>
              ) : null}
            </CTableBody>
          </CTable>
        </CCardBody>
      </CCard>
    </>
  )
}

export default Payments