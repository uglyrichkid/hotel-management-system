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
import StatusBadge from '../../components/common/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { bookingApi, paymentApi } from '../../services/api'

const initialPaymentForm = {
  amount: '',
  method: 'CARD',
  notes: '',
}

const money = (value) => {
  const number = Number(value || 0)
  return number.toFixed(2)
}

const moneyWithCurrency = (value, currencyCode) => {
  const number = Number(value || 0).toFixed(2)
  return currencyCode ? `${number} ${currencyCode}` : number
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
  if (booking?.guestFullName) return booking.guestFullName
  if (booking?.guestName) return booking.guestName

  const fullName = `${booking?.guestFirstName || ''} ${booking?.guestLastName || ''}`.trim()
  return fullName || '—'
}

const getRoomItems = (booking) => {
  if (Array.isArray(booking?.rooms)) return booking.rooms
  return []
}

const getNights = (booking) => {
  if (!booking?.checkInDate || !booking?.checkOutDate) return 0

  const start = new Date(booking.checkInDate)
  const end = new Date(booking.checkOutDate)
  const diff = end.getTime() - start.getTime()

  return Math.max(Math.round(diff / (1000 * 60 * 60 * 24)), 0)
}

const BookingManage = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const { token, selectedHotelId, selectedHotelName } = useAuth()

  const [booking, setBooking] = useState(null)
  const [financial, setFinancial] = useState(null)
  const [payments, setPayments] = useState([])

  const [loading, setLoading] = useState(false)
  const [actionLoading, setActionLoading] = useState(false)
  const [paymentsLoading, setPaymentsLoading] = useState(false)

  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [showPaymentModal, setShowPaymentModal] = useState(false)
  const [paymentForm, setPaymentForm] = useState(initialPaymentForm)
  const [submittingPayment, setSubmittingPayment] = useState(false)

  const bookingId = Number(id)

  const nights = useMemo(() => getNights(booking), [booking])
  const bookingCurrency = booking?.currencyCode || financial?.currencyCode || ''
  const remainingAmount = Number(financial?.remaining || 0)
  const hasDebt = remainingAmount > 0
  const canCheckOut = booking?.status === 'CHECKED_IN' && !hasDebt

  const loadBooking = async () => {
    if (!selectedHotelId || !bookingId) return

    const data = await bookingApi.getById(selectedHotelId, bookingId, token)
    setBooking(data)
  }

  const loadFinancialAndPayments = async () => {
    if (!bookingId) return

    setPaymentsLoading(true)

    try {
      const [financialData, paymentList] = await Promise.all([
        bookingApi.getFinancial(bookingId, token),
        bookingApi.getPayments(bookingId, token),
      ])

      setFinancial(financialData || null)
      setPayments(Array.isArray(paymentList) ? paymentList : [])
    } finally {
      setPaymentsLoading(false)
    }
  }

  const loadPage = async () => {
    if (!selectedHotelId || !bookingId) return

    try {
      setLoading(true)
      setError('')
      setSuccess('')

      await Promise.all([loadBooking(), loadFinancialAndPayments()])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadPage()
  }, [selectedHotelId, bookingId])

  const runAction = async (actionKey) => {
    if (!selectedHotelId || !bookingId) return

    try {
      setActionLoading(true)
      setError('')
      setSuccess('')

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

      await loadPage()
      setSuccess(`Booking ${actionKey} completed successfully.`)
    } catch (err) {
      setError(err.message)
    } finally {
      setActionLoading(false)
    }
  }

  const openPayFullModal = () => {
    setPaymentForm({
      amount: financial?.remaining ? String(financial.remaining) : '',
      method: 'CARD',
      notes: '',
    })
    setShowPaymentModal(true)
  }

  const openPayCustomModal = () => {
    setPaymentForm({
      amount: '',
      method: 'CARD',
      notes: '',
    })
    setShowPaymentModal(true)
  }

  const submitPayment = async (event) => {
    event.preventDefault()

    if (!bookingId) return

    try {
      setSubmittingPayment(true)
      setError('')
      setSuccess('')

      const body = {
        bookingId,
        amount: Number(paymentForm.amount),
        notes: paymentForm.notes || null,
      }

      if (paymentForm.method === 'CASH') {
        await paymentApi.payCash(body, token)
      } else {
        await paymentApi.payCard(
          {
            ...body,
            transactionReference: null,
          },
          token,
        )
      }

      setShowPaymentModal(false)
      setPaymentForm(initialPaymentForm)

      await Promise.all([loadBooking(), loadFinancialAndPayments()])
      setSuccess('Payment processed successfully.')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmittingPayment(false)
    }
  }

  if (!selectedHotelId) {
    return (
      <>
        <PageHeader title="Booking details" subtitle="Booking context is required." />
        <SelectedHotelAlert hotelName={selectedHotelName} />
      </>
    )
  }

  return (
    <>
      <PageHeader
        title={booking ? `Booking #${booking.id}` : 'Booking details'}
        subtitle="Reservation profile with stay details, guest data, lifecycle actions and embedded payment flow."
        actionLabel="Back to bookings"
        actionColor="secondary"
        onAction={() => navigate('/bookings')}
      />

      {error ? <CAlert color="danger">{error}</CAlert> : null}
      {success ? <CAlert color="success">{success}</CAlert> : null}

      {loading ? (
        <CCard className="mb-4">
          <CCardBody className="text-center py-5">
            <CSpinner color="primary" />
          </CCardBody>
        </CCard>
      ) : null}

      {!loading && booking ? (
        <>
          <CRow className="g-4 mb-4">
            <CCol lg={8}>
              <CCard className="h-100">
                <CCardHeader>Booking overview</CCardHeader>
                <CCardBody>
                  <CRow className="g-4">
                    <CCol md={6}>
                      <div className="mb-3">
                        <div className="text-body-secondary small">Guest</div>
                        <div className="fw-semibold">{getGuestName(booking)}</div>
                        <div className="small text-body-secondary">{booking.guestEmail || '—'}</div>
                        <div className="small text-body-secondary">{booking.guestPhone || '—'}</div>
                      </div>

                      <div className="mb-3">
                        <div className="text-body-secondary small">Stay dates</div>
                        <div className="fw-semibold">
                          {booking.checkInDate || '—'} → {booking.checkOutDate || '—'}
                        </div>
                        <div className="small text-body-secondary">{nights} nights</div>
                      </div>

                      <div className="mb-3">
                        <div className="text-body-secondary small">Occupancy</div>
                        <div className="fw-semibold">
                          {booking.adults || 0} adults / {booking.children || 0} children
                        </div>
                      </div>
                    </CCol>

                    <CCol md={6}>
                      <div className="mb-3">
                        <div className="text-body-secondary small mb-1">Booking status</div>
                        <StatusBadge value={booking.status} />
                      </div>

                      <div className="mb-3">
                        <div className="text-body-secondary small mb-1">Payment status</div>
                        <StatusBadge value={booking.paymentStatus || financial?.status} />
                      </div>

                      <div className="mb-3">
                        <div className="text-body-secondary small">Total booking value</div>
                        <div className="fw-semibold">
                          {moneyWithCurrency(booking.totalPrice, bookingCurrency)}
                        </div>
                      </div>

                      <div className="mb-3">
                        <div className="text-body-secondary small">Notes</div>
                        <div>{booking.notes || '—'}</div>
                      </div>
                    </CCol>
                  </CRow>

                  <div className="d-flex flex-wrap gap-2 mt-3">
                    {booking.status === 'CREATED' ? (
                      <CButton
                        color="info"
                        variant="outline"
                        disabled={actionLoading}
                        onClick={() => runAction('confirm')}
                      >
                        {actionLoading ? 'Working...' : 'Confirm'}
                      </CButton>
                    ) : null}

                    {booking.status === 'CONFIRMED' ? (
                      <CButton
                        color="primary"
                        variant="outline"
                        disabled={actionLoading}
                        onClick={() => runAction('check-in')}
                      >
                        {actionLoading ? 'Working...' : 'Check-in'}
                      </CButton>
                    ) : null}

                    {booking.status === 'CHECKED_IN' ? (
                      <div className="d-flex flex-column gap-2">
                        <CButton
                          color="success"
                          variant="outline"
                          disabled={actionLoading || !canCheckOut}
                          onClick={() => runAction('check-out')}
                        >
                          {actionLoading ? 'Working...' : 'Check-out'}
                        </CButton>

                        {hasDebt ? (
                          <div className="small text-danger">
                            Check-out is blocked until the remaining balance is fully paid.
                          </div>
                        ) : null}
                      </div>
                    ) : null}

                    {booking.status !== 'CANCELLED' && booking.status !== 'CHECKED_OUT' ? (
                      <CButton
                        color="danger"
                        variant="outline"
                        disabled={actionLoading}
                        onClick={() => runAction('cancel')}
                      >
                        {actionLoading ? 'Working...' : 'Cancel booking'}
                      </CButton>
                    ) : null}
                  </div>
                </CCardBody>
              </CCard>
            </CCol>

            <CCol lg={4}>
              <CCard className="h-100">
                <CCardHeader>Financial summary</CCardHeader>
                <CCardBody>
                  {paymentsLoading ? <CSpinner size="sm" color="primary" /> : null}

                  <div className="mb-3">
                    <div className="text-body-secondary small">Total</div>
                    <div className="fs-5 fw-semibold">
                      {moneyWithCurrency(financial?.total ?? booking.totalPrice, bookingCurrency)}
                    </div>
                  </div>

                  <div className="mb-3">
                    <div className="text-body-secondary small">Paid</div>
                    <div className="fs-5 fw-semibold">
                      {moneyWithCurrency(financial?.paid ?? booking.paidAmount, bookingCurrency)}
                    </div>
                  </div>

                  <div className="mb-3">
                    <div className="text-body-secondary small">Remaining</div>
                    <div className="fs-4 fw-bold">
                      {moneyWithCurrency(financial?.remaining, bookingCurrency)}
                    </div>
                  </div>

                  <div className="mb-4">
                    <div className="text-body-secondary small mb-1">Financial status</div>
                    <StatusBadge value={financial?.status || booking.paymentStatus} />
                  </div>

                  {Number(financial?.remaining || 0) > 0 ? (
                    <div className="d-grid gap-2">
                      <CButton color="primary" onClick={openPayFullModal}>
                        Pay full amount
                      </CButton>
                      <CButton color="secondary" variant="outline" onClick={openPayCustomModal}>
                        Pay custom amount
                      </CButton>
                    </div>
                  ) : (
                    <CAlert color="success" className="mb-0">
                      This booking is fully paid.
                    </CAlert>
                  )}
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>

          <CRow className="g-4 mb-4">
            <CCol lg={6}>
              <CCard className="h-100">
                <CCardHeader>Assigned rooms</CCardHeader>
                <CCardBody>
                  <CTable responsive hover>
                    <CTableHead>
                      <CTableRow>
                        <CTableHeaderCell>Room</CTableHeaderCell>
                        <CTableHeaderCell>Type</CTableHeaderCell>
                        <CTableHeaderCell>Floor</CTableHeaderCell>
                        <CTableHeaderCell>Capacity</CTableHeaderCell>
                      </CTableRow>
                    </CTableHead>
                    <CTableBody>
                      {getRoomItems(booking).map((room, index) => (
                        <CTableRow key={room.id || `${room.roomNumber}-${index}`}>
                          <CTableDataCell>{room.roomNumber || room.number || '—'}</CTableDataCell>
                          <CTableDataCell>{room.roomTypeName || room.roomType || '—'}</CTableDataCell>
                          <CTableDataCell>{room.floor ?? '—'}</CTableDataCell>
                          <CTableDataCell>{room.capacity ?? '—'}</CTableDataCell>
                        </CTableRow>
                      ))}

                      {getRoomItems(booking).length === 0 ? (
                        <CTableRow>
                          <CTableDataCell colSpan={4} className="text-center text-body-secondary py-4">
                            No rooms found in booking response.
                          </CTableDataCell>
                        </CTableRow>
                      ) : null}
                    </CTableBody>
                  </CTable>
                </CCardBody>
              </CCard>
            </CCol>

            <CCol lg={6}>
              <CCard className="h-100">
                <CCardHeader>Operational notes</CCardHeader>
                <CCardBody>
                  <div className="mb-3">
                    <div className="text-body-secondary small">Hotel context</div>
                    <div className="fw-semibold">{selectedHotelName || `Hotel #${selectedHotelId}`}</div>
                  </div>

                  <div className="mb-3">
                    <div className="text-body-secondary small">Booking ID</div>
                    <div className="fw-semibold">#{booking.id}</div>
                  </div>

                  <div className="mb-3">
                    <div className="text-body-secondary small">Lifecycle warning</div>
                    {hasDebt ? (
                      <CAlert color="warning" className="mb-0">
                        Remaining balance: {moneyWithCurrency(remainingAmount, bookingCurrency)}. Check-out is blocked until full payment is completed.
                      </CAlert>
                    ) : (
                      <CAlert color="success" className="mb-0">
                        No financial blockers detected for this booking.
                      </CAlert>
                    )}
                  </div>
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>

          <CCard>
            <CCardHeader className="d-flex justify-content-between align-items-center">
              <span>Payment history</span>
              <CBadge color="light" textColor="dark">
                {payments.length} payments
              </CBadge>
            </CCardHeader>
            <CCardBody>
              {paymentsLoading ? <CSpinner size="sm" color="primary" /> : null}

              <CTable responsive hover align="middle">
                <CTableHead>
                  <CTableRow>
                    <CTableHeaderCell>ID</CTableHeaderCell>
                    <CTableHeaderCell>Amount</CTableHeaderCell>
                    <CTableHeaderCell>Method</CTableHeaderCell>
                    <CTableHeaderCell>Status</CTableHeaderCell>
                    <CTableHeaderCell>Paid at</CTableHeaderCell>
                    <CTableHeaderCell>Reference</CTableHeaderCell>
                    <CTableHeaderCell>Notes</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {payments.map((payment) => (
                    <CTableRow key={payment.id}>
                      <CTableDataCell>#{payment.id}</CTableDataCell>
                      <CTableDataCell>
{moneyWithCurrency(payment.amount, bookingCurrency)}                      </CTableDataCell>
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

                  {!paymentsLoading && payments.length === 0 ? (
                    <CTableRow>
                      <CTableDataCell colSpan={7} className="text-center text-body-secondary py-4">
                        No payments yet for this booking.
                      </CTableDataCell>
                    </CTableRow>
                  ) : null}
                </CTableBody>
              </CTable>
            </CCardBody>
          </CCard>

          <CModal visible={showPaymentModal} onClose={() => setShowPaymentModal(false)}>
            <CModalHeader>
              <CModalTitle>Process payment</CModalTitle>
            </CModalHeader>

            <CForm onSubmit={submitPayment}>
              <CModalBody>
                <div className="mb-3">
                  <div className="text-body-secondary small">Remaining</div>
                  <div className={`fs-4 fw-bold ${hasDebt ? 'text-danger' : 'text-success'}`}>
                    {moneyWithCurrency(remainingAmount, bookingCurrency)}
                  </div>
                </div>

                <div className="mb-3">
                  <CFormLabel>
                    Amount {bookingCurrency ? `(${bookingCurrency})` : ''}
                  </CFormLabel>
                  <CFormInput
                    type="number"
                    step="0.01"
                    min="0.01"
                    max={Number(financial?.remaining || 0)}
                    required
                    value={paymentForm.amount}
                    onChange={(e) =>
                      setPaymentForm((prev) => ({
                        ...prev,
                        amount: e.target.value,
                      }))
                    }
                  />
                </div>

                <div className="mb-3">
                  <CFormLabel>Method</CFormLabel>
                  <CFormSelect
                    value={paymentForm.method}
                    onChange={(e) =>
                      setPaymentForm((prev) => ({
                        ...prev,
                        method: e.target.value,
                      }))
                    }
                  >
                    <option value="CARD">Card</option>
                    <option value="CASH">Cash</option>
                  </CFormSelect>
                </div>

                <div className="mb-0">
                  <CFormLabel>Notes</CFormLabel>
                  <CFormInput
                    value={paymentForm.notes}
                    onChange={(e) =>
                      setPaymentForm((prev) => ({
                        ...prev,
                        notes: e.target.value,
                      }))
                    }
                    placeholder="Optional payment notes"
                  />
                </div>

                {paymentForm.method === 'CARD' ? (
                  <div className="small text-body-secondary mt-3">
                    Card payments are processed through backend terminal flow. UI will wait for result and then refresh booking financials.
                  </div>
                ) : null}
              </CModalBody>

              <CModalFooter>
                <CButton color="secondary" variant="outline" onClick={() => setShowPaymentModal(false)}>
                  Cancel
                </CButton>
                <CButton type="submit" color="primary" disabled={submittingPayment}>
                  {submittingPayment
                    ? paymentForm.method === 'CARD'
                      ? 'Processing payment...'
                      : 'Saving payment...'
                    : 'Confirm payment'}
                </CButton>
              </CModalFooter>
            </CForm>
          </CModal>
        </>
      ) : null}
    </>
  )
}

export default BookingManage